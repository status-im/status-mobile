(ns status-im.extensions.list
  (:require ["status-extension-todolist" :default status-extension-todolist]
            ["status-extension-standups" :default status-extension-standups]
            ["status-extension-tornado" :default status-extension-tornado]
            ["status-extension-uniswap" :default status-extension-uniswap]
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
                  :hooks       (bean/->clj status-extension-standups)}
                 {:id          "test.tornado-extension"
                  :color       "#94febf"
                  :icon        (js/require "../resources/images/extensions/tornado.jpg")
                  :name        "Tornado.cash"
                  :author      "andrey.stateofus.eth"
                  :version     "1.0"
                  :description "Test extension test test"
                  :theme       :dark
                  :hooks       (bean/->clj status-extension-tornado)}

                 {:id          "test.uniswap-extension"
                  :color       "#FDEAF1"
                  :icon        (js/require "../resources/images/extensions/uniswap.png")
                  :name        "Uniswap"
                  :author      "design.stateofus.eth"
                  :version     "1.0"
                  :description "Test extension uniswap dex"
                  :hooks       (bean/->clj status-extension-uniswap)}])
