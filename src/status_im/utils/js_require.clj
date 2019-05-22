(ns status-im.utils.js-require)

(defmacro js-require [module-name]
  (let [js-module (gensym)]
    `(let [~js-module (atom nil)]
       (fn []
         (if (deref ~js-module)
           (deref ~js-module)
           (reset! ~js-module (js/require ~module-name)))))))
