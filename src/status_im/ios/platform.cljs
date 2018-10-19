(ns status-im.ios.platform
  (:require [status-im.react-native.js-dependencies :as rn-dependencies]))

(def fonts
  {:default          {:font-weight "normal"}
   :medium           {:font-weight "500"
                      :letter-spacing 1}
   :bold             {:font-weight "bold"}
   :toolbar-title    {:font-weight "bold"}
   :toolbar-subtitle {:font-weight "normal"}
   :monospace        {:font-family "Menlo"
                      :font-weight "bold"}})

;; iPhone X dimensions
(def x-height 812)
(def xs-height 896)

(defn iphone-x-dimensions? []
  (let [{:keys [width height]} (-> (.-Dimensions rn-dependencies/react-native)
                                   (.get "window")
                                   (js->clj :keywordize-keys true))]
    (or (= height x-height) (= height xs-height))))

(def platform-specific
  {:fonts                        fonts
   :tabs                         {:tab-shadows? false}
   :chats                        {:action-button?       false
                                  :new-chat-in-toolbar? true
                                  :render-separator?    true}
   :contacts                     {:action-button?          false
                                  :new-contact-in-toolbar? true}
   :group-block-shadows?         false
   :discover                     {:uppercase-subtitles? true}
   :status-bar-default-height    (if (iphone-x-dimensions?) 0 20)})
