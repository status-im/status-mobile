(ns status-im.ios.platform
  (:require [status-im.components.styles :as styles]))

(def component-styles
  {:status-bar            {:default     {:height    20
                                         :bar-style "default"
                                         :color     styles/color-white}
                           :main        {:height    20
                                         :bar-style "default"
                                         :color     styles/color-white}
                           :transparent {:height    20
                                         :bar-style "light-content"
                                         :color     styles/color-transparent}
                           :modal       {:height    20
                                         :bar-style "light-content"
                                         :color     "#2f3031"}}
   :toolbar               {:border-bottom-color styles/color-gray3
                           :border-bottom-width 0.5}
   :sized-text            {:margin-top        -5
                           :additional-height 5}
   :actions-list-view     {:border-bottom-color styles/color-gray3
                           :border-bottom-width 0.5}
   :chat                  {:new-message {:border-top-color styles/color-gray3
                                         :border-top-width 0.5}}
   :discover             {:subtitle  {:color          styles/color-steel
                                      :font-size      13
                                      :letter-spacing 1}
                           :popular  {:border-radius 3
                                      :border-width  1
                                      :border-color  "#D7D7D7"}
                           :tag      {:flex-direction   "column"
                                      :background-color "rgb(227, 235, 250)"
                                      :border-radius    4
                                      :border-width     1
                                      :border-color     "rgba(112, 153, 230, 0.31)"
                                      :padding          6}
                           :item     {:status-text {:color          styles/color-steel
                                                    :font-size      14
                                                    :letter-spacing -0.1}
                                      :icon        {:padding-top     0
                                                    :bottom          -4
                                                    :justify-content :flex-end}}}
   :contacts              {:subtitle     {:color          styles/color-black
                                          :font-size      16
                                          :letter-spacing -0.2}
                           :separator    {:margin-left      68
                                          :height           1
                                          :background-color styles/color-gray5
                                          :opacity          0.4}
                           :icon-check   {:border-radius 50
                                          :width         24
                                          :height        24}
                           :group-header {:flexDirection   :row
                                          :alignItems      :center
                                          :margin-top      24
                                          :height          53
                                          :backgroundColor styles/color-white}}
   :bottom-gradient       {:height 1}
   :input-label           {:left 0}
   :input-error-text      {:margin-left 0}
   :main-tab-list         {:margin-bottom 72}
   :toolbar-nav-action    {:width           46
                           :height          56
                           :align-items     :center
                           :justify-content :center}
   :toolbar-last-activity {:color            styles/text2-color
                           :background-color :transparent
                           :top              0
                           :font-size        14}
   :toolbar-title-container {:align-items :center}})

(def fonts
  {:light         {:font-family "SFUIText-Light"}
   :default       {:font-family "SFUIText-Regular"}
   :medium        {:font-family "SFUIText-Medium"}
   :bold          {:font-family "SFUIText-Bold"}

   :toolbar-title {:font-family "SFUIText-Medium"}})

;; Dialogs

(def react-native (js/require "react-native"))

(defn show-action-sheet [{:keys [options callback cancel-text]}]
  (.showActionSheetWithOptions (.-ActionSheetIOS react-native)
                               (clj->js {:options           (conj options cancel-text)
                                         :cancelButtonIndex (count options)})
                               callback))

;; Structure to be exported

(def platform-specific
  {:component-styles             component-styles
   :fonts                        fonts
   :list-selection-fn            show-action-sheet
   :tabs                         {:tab-shadows? false}
   :chats                        {:action-button?       false
                                  :new-chat-in-toolbar? true}
   :contacts                     {:action-button?          false
                                  :new-contact-in-toolbar? true
                                  :uppercase-subtitles?    true
                                  :group-block-shadows?    false}
   :discover                     {:uppercase-subtitles? true}
   :public-group-icon-container  {:margin-top 2}
   :private-group-icon-container {:margin-top 2}
   :public-group-chat-hash-style {:top 6 :left 3}})

