(ns status-im.android.platform)

(def fonts
  {:default          {:font-family "Roboto"}
   :medium           {:font-family "Roboto" :font-weight "500"}
   :bold             {:font-family "Roboto"
                      :font-weight "bold"}
   :toolbar-title    {:font-family "Roboto" :font-weight "500"}
   :toolbar-subtitle {:font-family "Roboto"}
   :monospace        {:font-family "monospace" :font-weight "bold"}})

;; Structure to be exported

(def platform-specific
  {:fonts                        fonts
   :tabs                         {:tab-shadows? true}
   :chats                        {:action-button?       true
                                  :new-chat-in-toolbar? false
                                  :render-separator?    false}
   :contacts                     {:action-button?          true
                                  :new-contact-in-toolbar? false}
   :group-block-shadows?         true
   :discover                     {:uppercase-subtitles? false}
   :status-bar-default-height    25})
