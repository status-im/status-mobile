(ns status-im.ios.platform
  (:require [status-im.components.styles :as styles]
            [status-im.utils.utils :as utils]))

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
   :toolbar-new           {:height              56
                           :padding-top         20
                           :padding-left        16
                           :padding-right       16}
   :toolbar-title-container {:align-items    :center}
   :toolbar-with-search-content {:align-items  :center}
   :sized-text            {:margin-top        -5
                           :additional-height 5}
   :actions-list-view     {:border-bottom-color styles/color-gray3
                           :border-bottom-width 0.5}
   :chat-list             {:list-container               {:background-color styles/color-white}
                           :chat-container               {:height 74}
                           :chat-icon-container          {:height 74}
                           :chat-info-container          {:margin-top 14}
                           :chat-options-container       {:margin-top 14}
                           :item-lower-container         {:margin-top 6}
                           :chat-name                    {:height 20}
                           :last-message                 {:font-size  15
                                                          :height     24}
                           :last-message-timestamp       {:font-size 15}
                           :unread-count                 {:top 3}
                           :public-group-icon-container  {:margin-top 2}
                           :private-group-icon-container {:margin-top 2}}
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
   :new-chat              {:option-inner-container {:height 64}
                           :option-icon-container  {:background-color styles/color-blue-transparent
                                                    :margin-top       12}
                           :option-name-text {:font-size 17
                                              :color     styles/color-light-blue}
                           :contact-list-title-container {:background-color styles/color-white
                                                          :padding-top      19
                                                          :padding-bottom   15
                                                          :margin-top       16}
                           :contact-list-title           {:color     styles/text1-color
                                                          :font-size 16}}
   :contacts              {:subtitle     {:color          styles/text1-color
                                          :font-size      16
                                          :letter-spacing -0.2}
                           :subtitle-count {:color          styles/color-gray4
                                            :font-size      16
                                            :letter-spacing -0.2}
                           :separator    {:margin-left      72
                                          :height           1
                                          :background-color styles/color-gray5
                                          :opacity          0.5}
                           :info-container {:margin-left 16}
                           :contact-inner-container {:height 63}
                           :icon-check   {:border-radius 50
                                          :width         24
                                          :height        24}
                           :group-header {:flexDirection   :row
                                          :alignItems      :center
                                          :margin-top      24
                                          :padding-left    16
                                          :padding-right   16
                                          :height          53
                                          :backgroundColor styles/color-white}
                           :show-all     {:padding-left    72
                                          :height          64}
                           :show-all-text {:fontSize       16
                                           :color          styles/color-gray4
                                           :letter-spacing -0.2}
                           :show-all-text-font :default
                           :contact-container {:padding-right 16}
                           :name-text {:fontSize       17
                                       :line-height    20
                                       :letter-spacing -0.2
                                       :color          styles/text1-color}}
   :new-group             {:group-name-text {:font-size 13}
                           :members-text {:letter-spacing -0.2
                                          :font-size      16}
                           :members-text-count  {:letter-spacing -0.2
                                                 :font-size      16}
                           :add-text {:margin-left    14
                                      :letter-spacing -0.2
                                      :font-size      17
                                      :line-height    20}
                           :contact-container {:height 63}
                           :settings-group-text {:color          styles/color-light-blue
                                                 :letter-spacing -0.2
                                                 :font-size      17
                                                 :line-height    20}
                           :settings-group-item {:padding-left   16
                                                 :height         64
                                                 :flex-direction :row
                                                 :align-items    :center}
                           :settings-group-container {:margin-top     25}
                           :settings-icon-container {:background-color "#628fe333"
                                                     :border-radius    50}
                           :delete-group-prompt-text {:font-size      14
                                                      :letter-spacing -0.2}
                           :delete-icon-container {:background-color "#d84b4b33"
                                                   :border-radius    50}}
   :reorder-groups        {:order-item-separator     {:margin-left      16
                                                      :opacity          0.5}
                           :order-item-container     {:height 63}
                           :order-item-icon          {:padding-right 20}
                           :order-item-label         {:padding-left   16
                                                      :font-size      17
                                                      :line-height    20
                                                      :letter-spacing -0.2}
                           :order-item-contacts      {:font-size      17
                                                      :line-height    20
                                                      :letter-spacing -0.2}}
   :confirm-button-label  {:color          styles/color-white
                           :font-size      17
                           :line-height    20
                           :letter-spacing -0.2}
   :bottom-gradient       {:height 1}
   :input-label           {:left 0}
   :input-error-text      {:margin-left 0}
   :toolbar-search-input  {:padding-left 10}
   :toolbar-nav-action    {:width           46
                           :height          56
                           :align-items     :center
                           :justify-content :center}
   :toolbar-border-container {:background-color styles/color-white}
   :toolbar-border        {:height           1
                           :background-color styles/color-gray5
                           :opacity          0.5}
   :toolbar-last-activity {:color            styles/text2-color
                           :background-color :transparent
                           :top              0
                           :font-size        14}})

(def fonts
  {:light         {:font-family "SFUIText-Light"}
   :default       {:font-family "SFUIText-Regular"}
   :medium        {:font-family "SFUIText-Medium"}
   :bold          {:font-family "SFUIText-Bold"}

   :toolbar-title {:font-family "SFUIText-Semibold"}})

;; Dialogs

(def react-native (js/require "react-native"))

(defn action-sheet-options [options]
  (let [destructive-opt-index (utils/first-index :destructive? options)]
    (clj->js (merge {:options           (mapv :text options)
                     :cancelButtonIndex (count options)}
                    (when destructive-opt-index {:destructiveButtonIndex destructive-opt-index})))))

(defn show-action-sheet [{:keys [options callback]}]
  (.showActionSheetWithOptions (.-ActionSheetIOS react-native)
                               (action-sheet-options options)
                               callback))

;; Structure to be exported

(def platform-specific
  {:component-styles             component-styles
   :fonts                        fonts
   :list-selection-fn            show-action-sheet
   :tabs                         {:tab-shadows? false}
   :chats                        {:action-button?       false
                                  :new-chat-in-toolbar? true
                                  :render-separator?    true}
   :uppercase?                   false
   :contacts                     {:action-button?          false
                                  :new-contact-in-toolbar? true
                                  :group-block-shadows?    false}
   :discover                     {:uppercase-subtitles? true}
   :public-group-icon-container  {:margin-top 2}
   :private-group-icon-container {:margin-top 2}
   :group-chat-focus-line-height 1
   :public-group-chat-hash-style {:top 6 :left 3}})
