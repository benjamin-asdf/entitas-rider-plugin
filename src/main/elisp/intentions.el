;; -*- lexical-binding: t; -*-

(require 'cl-lib)
(require 'cl-macs)
(require 's)
(require 'dash)
(require 'subr-x)

(defun idea/c-mode ()
  (unless (eq major-mode 'c-mode)
    (c-mode)
    (font-lock-mode -1)
    (setq
     indent-tabs-mode nil
     c-basic-offset 4
     tab-width 4)))

(defun idea/csharp-parse-parms (s)
  "Parse S as a csharp parameter list, return a list of lists of (type param-name)."
  (--map
   (list
    (progn
      (when (member
             (car it) (list "this" "ref" "out"))
        (pop it))
      (car it))
    (-second-item it))
   (--map
    (--map (s-chop-suffix "," it) it)
    (-partition-after-pred
     (lambda (it) (s-ends-with-p "," it))
     (nreverse
      (with-temp-buffer
        (insert s)
        (->gg)
	(re-collect "[a-zA-Z\.<,>]+")))))))



(defmacro idea/with-sanitized-region (start end &rest body)
  "Put the buffer substring between START and END into a temp buffer, remove chsarp comment syntax, then eval BODY."
  (declare (debug t) (indent 2))
  (with-gensyms (s)
    `(let ((,s (buffer-substring ,start ,end)))
       (with-temp-buffer
	 (insert ,s)
	 (->gg)
	 (while
             (re-search-forward "//" nil t)
           (delete-region
            (- (point) 2)
            (point-at-eol)))
	 (->gg)
	 ,@body))))



(defun idea/trace-log-method (&optional no-log-res)
  "Add csharp unity log syntax in method at point. When NO-LOG-RES is non-nil, only add at the entry of the method, not on the return."
  (interactive "P")
  (idea/c-mode)
  (let ((name)
	(args)
	(start)
	(this-unity-obj-part
	 (save-excursion
	   (->gg)
	   (if (re-search-forward
		(concat "public\\s-class\\>.*"
			(regexp-opt (list "MonoBehaviour" "ViewManager")))
		nil t)
	       ", this"
	     "")))
	(nullable-enabled
	 (save-excursion (->gg)
			 (re-search-forward "#nullable enable" nil t))))
    (-->
     (prog1
	 (or
	  (re-line
	   "\\<\\(\\w+\\)(\\(.+?\\)?)" t)
	  (user-error "Hover over the method"))
       (setf name (match-string-no-properties 1)
	     args (match-string-no-properties 2)))
     (idea/with-sanitized-region
	 (setq start (point-at-bol))
	 (progn
	   (skip-chars-forward "^{")
	   (forward-char -1)
	   (unless (looking-at-p " ")
	     (forward-char 1))
	   (forward-list)
	   (setq end (point)))
       (->gg)
       (idea/add-line
	(format
	 "UnityEngine.Debug.Log(($\"-> (%s%s)%s\")%s);"
	 name
	 (if args
	     (concat
	      " "
	      (s-join
	       " "
	       (--map
		(format "{%s}"
			(pcase (car it)
			  ("IEntity" (concat (cadr it) ".ToJsonPretty()"))
			  ("Contexts" (format "(%1$s == null ? 0.ToString() : %1$s.GetContextsName())" (cadr it)))
			  (_ (cadr it))))
		(idea/csharp-parse-parms
		 args))))
	   "")
	 (if
	     (not (string-empty-p this-unity-obj-part))
	     " {name}"
	   "")
	 this-unity-obj-part))
       (->gg)
       (unless no-log-res
	 (let ((nil-returns -1))
	   (cl-labels
	       ((insert-return-part
		 (it)
		 (idea/add-line
		  (format
		   "UnityEngine.Debug.Log($\"<- %s: %s\");\n%s"
		   name
		   (if it (format "{%s} #%s"
				  it
				  (s-chop-prefix "res_" (mkstr it)))
		     (format "nil #%d"
			     (cl-incf nil-returns)))
		   (format "return%s;"
			   (if it (format " %s" it) ""))))))
	     (unless
		 (while-re
		     "^[[:blank:]]+return\\(.+?\\)?;"
		   (-->
		    (prog1
			(match-string-no-properties 1)
		      (delete-region
		       (point-at-bol)
		       (point-at-eol)))
		    (unless (s-blank-str? it) it)
		    (progn
		      (forward-line -1)
		      (when it
			(let ((sym
			       (gensym "res_")))
			  (idea/add-line
			   (format "%s %s = %s;"
				   (if
				       (string-match-p "ActionResult" it)
				       (format
					"(ActionResult, System.Object%s)"
					(if nullable-enabled "?" ""))
				     "var")
				   sym
				   (s-trim it)))
			  sym)))
		    (insert-return-part it)))
	       (goto-char (point-max))
	       (forward-line -1)
	       (insert-return-part nil)))))
       (buffer-string))
     (progn
       (delete-region start end)
       (insert it)
       ;; (idea/indent-region-line-by-line start (point))
       (indent-region-line-by-line start (point))))))


(provide 'intentions)
