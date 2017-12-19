(ns status-im.test.protocol.node
  (:require [clojure.string :as s]
            [status-im.test.protocol.utils :as utils]))

(def build-dir "target")

(defonce node-process (atom nil))

(defn prepare-env! []
  (when-not (utils/exist? build-dir)
    (println "mkdir " build-dir)
    (utils/mkdir-sync build-dir))
  (let [dir  (s/join "/" [build-dir "status-go"])
        opts #js {:cwd dir}]
    (if-not (utils/exist? dir)
      (utils/exec-sync "git clone https://github.com/status-im/status-go.git -b develop" #js {:cwd build-dir})
      (utils/exec-sync "git pull origin develop" opts))
    (println "Compile statusgo...")
    (utils/exec-sync "make statusgo" opts)
    (println "Done.")))

(defn start! []
  (when-not @node-process
    (println "Start statusd...")
    (let [dir (s/join "/" [build-dir "status-go" "build" "bin"])]
      (let [proc (utils/spawn "./statusd"
                              ["--http" "--httpport" "8645" "-shh" "-logfile" "statusd.log"]
                              {:cwd dir})]
        (reset! node-process proc)
        (utils/sleep 5)
        (println "Done.")))))


(defn stop! []
  (println "Stop statusd...")
  (.kill @node-process)
  (println "Done.")
  (reset! node-process nil))

(def identity-1 "0x04eedbaafd6adf4a9233a13e7b1c3c14461fffeba2e9054b8d456ce5f6ebeafadcbf3dce3716253fbc391277fa5a086b60b283daf61fb5b1f26895f456c2f31ae3")
(def identity-2 "0x0490161b00f2c47542d28c2e8908e77159b1720dccceb6393d7c001850122efc3b1709bcea490fd8f5634ba1a145aa0722d86b9330b0e39a8d493cb981fd459da2")
(def topic-1 "0xdeadbeef")

(def topic-2 "0xbeefdead")
