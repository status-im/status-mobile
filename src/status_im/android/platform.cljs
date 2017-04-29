(ns status-im.android.platform
  (:require [status-im.components.styles :as styles]))

(def component-styles
  {:status-bar            {:default     {:height       25
                                         :bar-style    "dark-content"
                                         :elevation    2
                                         :translucent? true
                                         :color        styles/color-white}
                           :main        {:height            25
                                         :bar-style         "dark-content"
                                         :translucent?      true
                                         :color             styles/color-white
                                         :expandable-offset 3}
                           :transparent {:height       25
                                         :bar-style    "light-content"
                                         :translucent? true
                                         :color        styles/color-transparent}
                           :modal       {:height       0
                                         :bar-style    "light-content"
                                         :color        styles/color-black}
                           :transaction {:height       0
                                         :bar-style    "light-content"
                                         :color        styles/color-dark-blue-2}}
   :sized-text            {:margin-top        0
                           :additional-height 0}
   :chat                  {:new-message {:border-top-color styles/color-transparent
                                         :border-top-width 0.5}}
   :discover              {:subtitle {:color     styles/color-gray2
                                      :font-size 14}
                           :popular  {:border-radius 1
                                      :margin-top    2
                                      :margin-bottom 4
                                      :margin-right  2
                                      :elevation     2}
                           :tag      {:flex-direction   "column"
                                      :background-color "#7099e619"
                                      :border-radius    5
                                      :padding          4}
                           :item     {:status-text {:color       styles/color-black
                                                    :line-height 22
                                                    :font-size   14}}}
   :contacts              {:show-all-text-font :medium}
   :bottom-gradient       {:height 3}
   :input-label           {:left 4}
   :input-error-text      {:margin-left 4}
   :toolbar-nav-action    {:width           56
                           :height          56
                           :align-items     :center
                           :justify-content :center}
   :text-field-focus-line-height 2})

(def fonts
  {:light         {:font-family "Roboto-Light"}
   :default       {:font-family "Roboto-Regular"}
   :medium        {:font-family "Roboto-Medium"}

   :toolbar-title {:font-family "Roboto-Regular"}
   :roboto-mono   {:font-family "RobotoMono-Medium"}})

;; Dialogs

(def react-native-dialogs (js/require "react-native-dialogs"))

(defn show-dialog [{:keys [title options callback]}]
  (let [dialog (new react-native-dialogs)]
    (.set dialog (clj->js {:title         title
                           :items         (mapv :text options)
                           :itemsCallback callback}))
    (.show dialog)))


;; Structure to be exported

(def platform-specific
  {:component-styles             component-styles
   :fonts                        fonts
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
   :public-group-icon-container  {:margin-top 4}
   :private-group-icon-container {:margin-top 6}})
