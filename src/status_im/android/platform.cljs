(ns status-im.android.platform)

(def fonts
  {:light         {:font-family "Roboto-Light"}
   :default       {:font-family "Roboto-Regular"}
   :medium        {:font-family "Roboto-Medium"}

   :toolbar-title {:font-family "Roboto-Regular"}
   :roboto-mono   {:font-family "RobotoMono-Medium"}})

;; Structure to be exported

(def platform-specific
  {:fonts                        fonts
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
