(ns status-im.ios.platform
  (:require [status-im.components.styles :as styles]
            [status-im.i18n :refer [label]]
            [status-im.utils.utils :as utils]))

(def component-styles
  {:status-bar            {:default     {:height    20
                                         :bar-style "default"
                                         :color     styles/color-white}
                           :main        {:height            20
                                         :bar-style         "default"
                                         :color             styles/color-white
                                         :expandable-offset 8}
                           :transparent {:height    20
                                         :bar-style "light-content"
                                         :color     styles/color-transparent}
                           :modal       {:height    20
                                         :bar-style "light-content"
                                         :color     "#2f3031"}
                           :transaction {:height    20
                                         :bar-style "light-content"
                                         :color     styles/color-transparent}}
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
   :contacts              {:show-all-text-font :default}
   :bottom-gradient       {:height 1}
   :input-label           {:left 0}
   :input-error-text      {:margin-left 0}
   :toolbar-nav-action    {:width           46
                           :height          56
                           :align-items     :center
                           :justify-content :center}
   :toolbar-border-container {:background-color styles/color-white}
   :toolbar-border        {:height           1
                           :background-color styles/color-gray5
                           :opacity          0.5}
   :text-field-focus-line-height 1})

(def fonts
  {:light         {:font-family "SFUIText-Light"}
   :default       {:font-family "SFUIText-Regular"}
   :medium        {:font-family "SFUIText-Medium"}
   :bold          {:font-family "SFUIText-Bold"}

   :toolbar-title {:font-family "SFUIText-Semibold"}
   :roboto-mono   {:font-family "RobotoMono-Medium"}})

;; Dialogs

(def react-native (js/require "react-native"))

(defn action-sheet-options [options]
  (let [destructive-opt-index (utils/first-index :destructive? options)
        cancel-option         {:text (label :t/cancel)}
        options               (conj options cancel-option)]
    (clj->js (merge {:options           (mapv :text options)
                     :cancelButtonIndex (dec (count options))}
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
                                  :new-contact-in-toolbar? true}
   :group-block-shadows?         false
   :discover                     {:uppercase-subtitles? true}
   :public-group-icon-container  {:margin-top 2}
   :private-group-icon-container {:margin-top 2}})
