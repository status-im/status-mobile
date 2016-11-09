(ns status-im.android.platform
  (:require [status-im.components.styles :as styles]
            [status-im.utils.utils :as u]
            [status-im.components.toolbar.styles :refer [toolbar-background2]]))

(def component-styles
  {:status-bar            {:default     {:height    0
                                         :bar-style "dark-content"
                                         :color     styles/color-white}
                           :main        {:height    0
                                         :bar-style "dark-content"
                                         :color     toolbar-background2}
                           :transparent {:height       20
                                         :bar-style    "light-content"
                                         :translucent? true
                                         :color        styles/color-transparent}}
   :sized-text            {:margin-top        0
                           :additional-height 0}
   :chat                  {:new-message {:border-top-color styles/color-transparent
                                         :border-top-width 0.5}}
   :bottom-gradient       {:height 3}
   :input-label           {:left 4}
   :input-error-text      {:margin-left 4}
   :toolbar-nav-action    {:width           56
                           :height          56
                           :align-items     :center
                           :justify-content :center}
   :toolbar-last-activity {:color            styles/text2-color
                           :background-color :transparent
                           :top              0
                           :font-size        12}})

(def fonts
  {:default       {:font-family "sans-serif"}
   :medium        {:font-family "sans-serif-medium"}

   :toolbar-title {:font-family "sans-serif"}})


;; Dialogs

(def react-native-dialogs (js/require "react-native-dialogs"))

(defn show-dialog [{:keys [title options callback]}]
  (let [dialog (new react-native-dialogs)]
    (.set dialog (clj->js {:title         title
                           :items         options
                           :itemsCallback callback}))
    (.show dialog)))


;; Structure to be exported

(def platform-specific
  {:component-styles  component-styles
   :fonts             fonts
   :list-selection-fn show-dialog
   :chats             {:action-button?       true
                       :new-chat-in-toolbar? false}
   :contacts          {:action-button?          true
                       :new-contact-in-toolbar? false}})
