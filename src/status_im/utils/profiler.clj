(ns status-im.utils.profiler)

(defmacro with-measure
  [name & body]
  `(let [start# (js/performance.now)
         res#   (do ~@body)
         end#   (js/performance.now)
         time#  (.toFixed (- end# start#) 2)]
     (taoensso.timbre/info "[perf|" ~name "] => " time#)
     res#))
