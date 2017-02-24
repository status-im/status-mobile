(ns status-im.android.platform
  (:require [status-im.components.styles :as styles]))

(def component-styles
  {:status-bar            {:default     {:height    0
                                         :bar-style "dark-content"
                                         :color     styles/color-white}
                           :main        {:height    0
                                         :bar-style "dark-content"
                                         :color     styles/color-white}
                           :transparent {:height       20
                                         :bar-style    "light-content"
                                         :translucent? true
                                         :color        styles/color-transparent}
                           :modal       {:height    0
                                         :bar-style "light-content"
                                         :color     styles/color-black}}
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
   :contacts              {:subtitle     {:color     styles/color-gray4
                                          :font-size 14}
                           :separator    {:height 0}
                           :icon-check   {:border-radius 2
                                          :width         17
                                          :height        17}
                           :group-header {:flexDirection   :row
                                          :alignItems      :center
                                          :height          56
                                          :padding-top     10
                                          :backgroundColor styles/color-light-gray}}
   :bottom-gradient       {:height 3}
   :input-label           {:left 4}
   :input-error-text      {:margin-left 4}
   :main-tab-list         {:margin-bottom 20}
   :toolbar-nav-action    {:width           56
                           :height          56
                           :align-items     :center
                           :justify-content :center}
   :toolbar-last-activity {:color            styles/text2-color
                           :background-color :transparent
                           :top              0
                           :font-size        12}
   :toolbar-title-container {:padding-left   16}})

(def fonts
  {:light         {:font-family "sans-serif-light"}
   :default       {:font-family "sans-serif"}
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
  {:component-styles             component-styles
   :fonts                        fonts
   :list-selection-fn            show-dialog
   :tabs                         {:tab-shadows? true}
   :chats                        {:action-button?       true
                                  :new-chat-in-toolbar? true
                                  :render-separator?    false}
   :contacts                     {:action-button?          true
                                  :new-contact-in-toolbar? false
                                  :uppercase-subtitles?    false
                                  :group-block-shadows?    true}
   :discover                     {:uppercase-subtitles? false}
   :public-group-icon-container  {:margin-top 4}
   :private-group-icon-container {:margin-top 6}
   :public-group-chat-hash-style {:top 10 :left 4}})
