(ns cheshire.type-dispatch
  (:use [clojure.pprint :only [pprint]]))

;; OMG SCARY
;; not crazy about these names
;; should use a vector instead of a map so to having deterministic
;; ordering in generated condp
(defmacro deftypedispatched
  "generates macros for fast inlined type dispatching (like defmulti)"
  [name]
  `(do
     (defmacro ~name [type# & args#]
       (let [bodies# @(:cases (meta (resolve '~name)))]
         (list* 'condp 'instance? type#
                (concat (apply concat (for [[class# fn#] bodies#
                                            :when (not= class# :miss)]
                                        [class# (list* fn# type# args#)]))
                        (when (:miss bodies#)
                          [(list* (:miss bodies#) type# args#)])))))
     (alter-meta! (resolve '~name) assoc :cases (atom {}))
     (resolve '~name)))

(defmacro defdispatched
  "generates macros for fast inlined type dispatching (like defmethod), body is
  in the style of definline or defmacro"
  [a-name type args & body]
  (let [fn-name (symbol (str a-name "-" (if (keyword? type)
                                          (name type)
                                          (.getName (resolve type)))))]
      `(do
         (definline ~fn-name ~args
           (list 'let '[~(if (keyword? type)
                           (first args)
                           (vary-meta (first args) assoc :tag type))
                        ~(first args)
                        ~(second args) ~(second args)]
                 ~@body))
         (swap! (:cases (meta (resolve '~a-name))) assoc ~type '~fn-name)
         (resolve '~a-name))))
