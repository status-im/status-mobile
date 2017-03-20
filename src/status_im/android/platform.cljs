(ns status-im.android.platform
  (:require [status-im.components.styles :as styles]))

(def component-styles
  {:status-bar            {:default     {:height    0
                                         :bar-style "dark-content"
                                         :color     styles/color-white}
                           :main        {:height    0
                                         :bar-style "dark-content"
                                         :color     styles/color-white}
                           :gray        {:height    0
                                         :bar-style "dark-content"
                                         :color     styles/color-light-gray}
                           :transparent {:height       20
                                         :bar-style    "light-content"
                                         :translucent? true
                                         :color        styles/color-transparent}
                           :modal       {:height    0
                                         :bar-style "light-content"
                                         :color     styles/color-black}}
   :toolbar-new           {:height         55
                           :padding-top    15
                           :padding-left   16
                           :padding-right  16}
   :toolbar-title-container {:padding-left   30}
   :toolbar-with-search-content {:padding-left   30}
   :sized-text            {:margin-top        0
                           :additional-height 0}
   :chat-list             {:list-container               {:background-color styles/color-light-gray}
                           :chat-container               {:height 76}
                           :chat-icon-container          {:height 76}
                           :chat-info-container          {:margin-top 16}
                           :chat-options-container       {:margin-top 16}
                           :item-lower-container         {:margin-top 4}
                           :chat-name                    {:height 24}
                           :last-message                 {:font-size  14
                                                          :height     24}
                           :last-message-timestamp       {:font-size 14}
                           :unread-count                 {:top 2}
                           :public-group-icon-container  {:margin-top 4}
                           :private-group-icon-container {:margin-top 4}}
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
   :new-chat              {:option-inner-container       {:height 56}
                           :option-icon-container        {:background-color styles/color-white
                                                          :margin-top       8}
                           :option-name-text             {:font-size 16
                                                          :color     styles/text1-color}
                           :contact-list-title-container {:background-color styles/color-light-gray
                                                          :padding-top      28
                                                          :padding-bottom   20
                                                          :margin-top       0}
                           :contact-list-title           {:color     styles/text4-color
                                                          :font-size 14}}
   :contacts              {:subtitle     {:color     styles/color-gray4
                                          :font-size 14}
                           :subtitle-count {:color     styles/color-gray4
                                            :font-size 14}
                           :info-container {:margin-left 16}
                           :contact-inner-container {:height 56}
                           :contact-list-spacing {:background-color styles/color-white
                                                  :height           8}
                           :icon-check   {:border-radius 2
                                          :width         17
                                          :height        17}
                           :group-header {:flexDirection   :row
                                          :alignItems      :center
                                          :height          56
                                          :padding-top     10
                                          :padding-left    16
                                          :padding-right   14
                                          :backgroundColor styles/color-light-gray}
                           :show-all     {:padding-left    72
                                          :height          56}
                           :show-all-text {:fontSize       14
                                           :color          styles/color-blue
                                           :letter-spacing 0.5}
                           :show-all-text-font :medium
                           :contact-container {:padding-right 16}
                           :name-text {:fontSize       16
                                       :line-height    24
                                       :color          styles/text1-color}}
   :profile               {:profile-bage {:padding-top 24}
                           :profile-name-text {:color       styles/color-black
                                               :font-size   16
                                               :line-height 24}
                           :profile-status-text {:font-size   15
                                                 :line-height 20}
                           :profile-setting-item {:padding-left  72
                                                  :padding-right 13
                                                  :height        72}
                           :profile-setting-title {:font-size 12}
                           :profile-setting-text {:font-size 16
                                                  :color     styles/color-black}
                           :profile-setting-spacing {:height 7}
                           :form-separator {:margin-left 72}
                           :edit-name-title {:font-size 12}
                           :profile-icon-edit-text {:font-size 12}}
   :new-group             {:group-name-text {:font-size 12}
                           :members-text {:font-size 14}
                           :members-text-count  {:font-size 14}
                           :add-text {:margin-left    16
                                      :line-height    24
                                      :font-size      16}
                           :contact-container {:height 56}
                           :settings-group-text {:letter-spacing 0.5
                                                 :font-size      16}
                           :delete-group-prompt-text {:font-size 12}
                           :settings-group-item {:padding-left   16
                                                 :height         56
                                                 :flex-direction :row
                                                 :align-items    :center}
                           :settings-group-container {:margin-top    23}}
   :reorder-groups        {:order-item-container     {:height           56
                                                      :background-color styles/color-white}
                           :order-item-icon          {:padding-right 16}
                           :order-item-label         {:padding-left   16
                                                      :font-size      16
                                                      :color          styles/color-black
                                                      :line-height    24}
                           :reorder-list-container   {:padding-top 16}
                           :order-item-contacts      {:font-size   16
                                                      :line-height 24}
                           :add-to-contacts-text     {:font-size      14
                                                      :letter-spacing 0.5}}
   :confirm-button-label  {:color          styles/color-white
                           :font-size      14
                           :letter-spacing 0.5}
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
                           :font-size        12}
   :text-field-focus-line-height 2})

(def fonts
  {:light         {:font-family "Roboto-Light"}
   :default       {:font-family "Roboto-Regular"}
   :medium        {:font-family "Roboto-Medium"}

   :toolbar-title {:font-family "Roboto-Regular"}})

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
   :private-group-icon-container {:margin-top 6}
   :public-group-chat-hash-style {:top 10 :left 4}})
