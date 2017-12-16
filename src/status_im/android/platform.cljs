(ns status-im.android.platform
  (:require [status-im.react-native.js-dependencies :as rn-dependencies]))

(def fonts
  {:light         {:font-family "Roboto-Light"}
   :default       {:font-family "Roboto-Regular"}
   :medium        {:font-family "Roboto-Medium"}

   :toolbar-title {:font-family "Roboto-Regular"}
   :roboto-mono   {:font-family "RobotoMono-Medium"}})

;; Dialogs

(defn show-dialog [{:keys [title options callback]}]
  (let [dialog (new rn-dependencies/dialogs)]
    (.set dialog (clj->js {:title         title
                           :items         (mapv :text options)
                           :itemsCallback callback}))
    (.show dialog)))


;; Structure to be exported

(def platform-specific
  {:fonts                        fonts
   :list-selection-fn            show-dialog
   :tabs                         {:tab-shadows? true}
   :chats                        {:action-button?       true
                                  :new-chat-in-toolbar? false
                                  :render-separator?    false}
   :uppercase?                   true
   :contacts                     {:action-button?          true
                                  :new-contact-in-toolbar? false}
   :group-block-shadows?         true
   :discover                     {:uppercase-subtitles? false}
   :status-bar-default-height    25})
