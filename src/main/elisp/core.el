;; -*- lexical-binding: t; -*-

(require 'cl-macs)
(require 'cl-lib)

(defmacro awhen (test then-form)
  (declare(debug t))
  `(let ((it ,test))
     (when it ,then-form)))

(defmacro with-gensyms (symbols &rest body)
  "Bind the SYMBOLS to fresh uninterned symbols and eval BODY."
  (declare (indent 1))
  `(let ,(mapcar (lambda (s)
                   `(,s (cl-gensym (symbol-name ',s))))
                 symbols)
     ,@body))

(defsubst ->gg ()
  (goto-char (point-min)))

(defsubst re-line (re &optional noerror count)
  (beginning-of-line)
  (re-search-forward re (point-at-eol) noerror count))

(defun re-collect (re &optional bound count num)
  (let ((lst '()))
    (while (re-search-forward re bound t count)
      (push (match-string-no-properties (or num 0)) lst))
    lst))


(defun while-re--build-call (spec)
  `(apply
    #'re-search-forward
    ',(if (stringp spec)
	 `(,spec nil t)
       spec)))

(defmacro while-re (re-spec &rest body)
  "Execute BODY after each match with RE-SPEC.
RE-SPEC can be a string or a list of args applied to `re-search-forward'.
Return t, if there was any match, nil otherwise."
  (declare (debug t) (indent 1))
  (with-gensyms (any)
    `(let ((,any nil))
       (while
	   ,(while-re--build-call re-spec)
	 (setf ,any t)
	 ,(when body
	    `(save-excursion ,@body)))
       ,any)))




(defun indent-region-line-by-line (start end)
  (save-excursion
    (goto-char start)
    (while (< (point) end)
      (or (and (bolp) (eolp))
	  (indent-according-to-mode))
      (forward-line 1))))

(defun idea/add-line (string)
  "Insert STRING in new line."
  (goto-char (point-at-eol))
  (newline)
  (let ((start (point-marker)))
    (insert string)
    (indent-region-line-by-line start (point))))



(defun idea/log (format-string &rest args)
  (when (file-exists-p "/tmp/")
    (let ((file "/tmp/idea-idle-emacs-log"))
      (unless (file-exists-p file)
	(write-region "" nil file))
      (with-current-buffer
	  (find-file-noselect file t t)
	(goto-char (point-max))
	(insert (apply #'format format-string args) "\n")
	(save-buffer)))))

(defvar idea/arg-data '())

(defun idea/main (&optional args)
  (setf args (or args idea/arg-data))
  (idea/log
   (with-output-to-string
     (print (list (or buffer-file-name "no-file") args))))
  (awhen
   (plist-get args :idea-point)
   (goto-char (1+ it)))
  (setq-local indent-line-function 'idea/indent))

(defun idea/print ()
  (goto-char (point-min))
  (save-excursion
    (while-re "\r\n"
      (replace-match "\n")))
  (narrow-to-region
   (save-excursion
     (when (while-re "^using")
       (forward-line 1))
     (skip-chars-forward "[^\t\n\r\s]")
     (point))
   (point-max))
  (princ
   (buffer-substring-no-properties (point-min) (point-max))))


(provide 'core)
