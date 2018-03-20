(ns status-im.cli.core
  (:require [cljs.nodejs :as nodejs]
            [status-im.utils.name :as name]))

(nodejs/enable-util-print!)

(defn -main [& args]
  (println (str "Hello world! args: " args))
  ;; just to show that we can use status-im.utils.* namespaces
  (println (name/shortened-name "Verylongnameazazazazazazazaz" 5)))

(set! *main-cli-fn* -main)
