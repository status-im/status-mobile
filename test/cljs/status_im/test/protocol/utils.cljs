(ns status-im.test.protocol.utils)

(def fs (js/require "fs"))
(def child-process (js/require "child_process"))
(def process (js/require "process"))

(def exist? (.-existsSync fs))

(defn exec-sync [command options]
  (.execSync child-process command (clj->js options)))

(defn exec [command options]
  (.exec child-process command (clj->js options)))

(defn spawn-sync [command args options]
  (.spawnSync child-process command (clj->js args) (clj->js options)))

(defn spawn [command args options]
  (.spawn child-process command (clj->js args) (clj->js options)))

(defn exit! []
  (.exit process))

(defn mkdir-sync [dir]
  (.mkdirSync fs dir))
