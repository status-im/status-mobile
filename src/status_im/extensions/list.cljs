(ns status-im.extensions.list
  (:require ["status-extension-todolist" :default status-extension-todolist]
            ["status-extension-standups" :default status-extension-standups]
            [cljs-bean.core :as bean]))

(def extensions [{:id          "test.todo-extension"
                  :color       "#7CDA00"
                  :icon        (js/require "../resources/images/extensions/todo.png")
                  :name        "ToDoList"
                  :author      "andrey.stateofus.eth"
                  :version     "1.0"
                  :description "Test extension test test"
                  :hooks       (bean/->clj status-extension-todolist)}
                 {:id          "test.standup-extension"
                  :color       "#8B3131"
                  :icon        (js/require "../resources/images/extensions/standup.png")
                  :name        "Standups"
                  :author      "andrey.stateofus.eth"
                  :version     "1.0"
                  :description "Test extension test test"
                  :hooks       (bean/->clj status-extension-standups)}])