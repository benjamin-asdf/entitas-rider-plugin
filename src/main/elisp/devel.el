;;; devel.el --- just elisp for ideaidle

;;; Commentary:
;;


(defun idea/rider-idea-log ()
  (interactive)
  (let ((default-directory "/home/benj/repos/kotlin/rider-sandbox/")
	(log-file "/home/benj/repos/kotlin/rider-sandbox/system/log/idea.log"))
    (unless (get-buffer "best-log")
      (with-current-buffer
	  (get-buffer-create "best-log")
	(async-shell-command
	 (concat
	  "tail -f "
	  log-file
	  " "
	  "| grep -e STDOUT -e clojure")
	 (current-buffer)
	 (current-buffer))
	(pop-to-buffer
	 (current-buffer))))))

(defun idea/rider-playground ()
  (interactive)
  (let ((default-directory "/home/benj/repos/kotlin/rider-sandbox/")
	(log-file "/home/benj/repos/kotlin/rider-sandbox/system/log/idea.log")
	(log-file-2 "/tmp/event.log")
	(proc-buff "rider-playground")
	(process-environment
	 '("IDEAIDLE_REPL_PORT=7888"
	   "IDEAIDLE_HOME=/home/benj/repos/kotlin/ideaidle/"
	   ;; "RIDER_JDK=/usr/lib/jvm/java-11-openjdk/"
	   )))
    (with-current-buffer (find-file-noselect log-file-2)
      (auto-revert-mode))
    (when (file-exists-p log-file)
      (delete-file log-file))
    (shell-command
     "rm -rf ./plugins/IdeaIdle")
    (shell-command
     (format
      "unzip %s -d ./plugins"
      "/home/benj/repos/kotlin/ideaidle/bin/IdeaIdle-0.0.1.zip"))
    (-some->>
	(get-buffer proc-buff)
      (kill-buffer))
    (async-shell-command
     (concat
      "./bin/rider.sh"
      " "
      "/home/benj/idlegame/RoslynAnalyzers/RoslynPlayground/RoslynPlayground.sln")
     proc-buff)))

(defun idea/cider-connect ()
  (interactive)
  (or (-some->>
	  (buffer-list)
	(--filter
	 (string-match-p
	  "*cider-repl.*7888"
	  (buffer-name it)))
	(car)
	(pop-to-buffer))
      (let ((default-directory "~/repos/kotlin/ideaidle/idlelib"))
	(cider-connect-clj
	 '(:host "localhost" :port 7888)))))


(defun idea/find-cider-repl (&optional arg)
  (interactive "P")
  (mememacs/helm-mini
   (concat "*cider-repl " (unless arg "!7888"))))


(defun mm/java-imports-to-edn (beg end)
  "Convert import syntax to edn used in clj imports."
  (interactive "r")
  (replace-regexp-in-region
   "import \\(.*\\)\\.\\(.*\\)"
   "[\\1 \\2]"
   beg end))

(mememacs/comma-def
  "tp"
  #'idea/rider-playground
  "tc"
  #'idea/cider-connect
  "ti"
  #'mm/java-imports-to-edn
  "tr"
  #'idea/find-cider-repl
  "td"
  (lambda ()
    (interactive)
    (let ((default-directory "~/repos/kotlin/ideaidle"))
      (projectile-dired)))
  "tC"
  (lambda ()
    (interactive)
    (pop-to-buffer
     (car
      (--filter
       (eq
	'compilation-mode
	(buffer-local-value 'major-mode it))
       (buffer-list))))))


(add-to-list
 'load-path
 (expand-file-name
  "~/repos/kotlin/ideaidle/src/main/elisp/"))


(add-to-list
'auto-mode-alist
'("\\.kt" . java-mode))


(require 'core)
(require 'intentions)


(provide 'devel)

;;; devel.el ends here
