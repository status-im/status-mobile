(ns quo2.components.notifications.toast
  (:require [utils.i18n :as i18n]
            [quo2.components.icon :as icon]
            [quo2.components.markdown.text :as text]
            [quo2.components.notifications.count-down-circle :as count-down-circle]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as theme]
            [react-native.core :as rn]))

(def ^:private themes
  {:container        {:dark  {:background-color colors/white-opa-70}
                      :light {:background-color colors/neutral-80-opa-70}}
   :text             {:dark  {:color colors/neutral-100}
                      :light {:color colors/white}}
   :icon             {:dark  {:color colors/neutral-100}
                      :light {:color colors/white}}
   :action-container {:dark  {:background-color :colors/neutral-80-opa-5}
                      :light {:background-color :colors/white-opa-5}}})

(defn- merge-theme-style
  [component-key styles]
  (merge (get-in themes [component-key (theme/get-theme)]) styles))

(defn toast-action-container
  [{:keys [on-press style]} & children]
  [rn/touchable-highlight
   {:on-press       on-press
    :underlay-color :transparent}
   [into
    [rn/view
     {:style (merge
              {:flex-direction     :row
               :padding-vertical   3
               :padding-horizontal 8
               :align-items        :center
               :border-radius      8
               :background-color   (get-in themes
                                           [:action-container (theme/get-theme)
                                            :background-color])}
              style)}]
    children]])

(defn toast-undo-action
  [duration on-press]
  [toast-action-container
   {:on-press on-press :accessibility-label :toast-undo-action}
   [rn/view {:style {:margin-right 5}}
    [count-down-circle/circle-timer {:duration duration}]]
   [text/text
    {:size :paragraph-2 :weight :medium :style (merge-theme-style :text {})}
    [i18n/label :t/undo]]])

(defn- toast-container
  [{:keys [left middle right]}]
  [rn/view {:style {:padding-left 12 :padding-right 12}}
   [rn/view
    {:style (merge-theme-style :container
                               {:flex-direction   :row
                                :width            "100%"
                                :margin           :auto
                                :justify-content  :space-between
                                :padding-vertical 8
                                :padding-left     10
                                :padding-right    8
                                :border-radius    12})}
    [rn/view {:style {:padding 2}} left]
    [rn/view {:style {:padding 4 :flex 1}}
     [text/text
      {:size                :paragraph-2
       :weight              :medium
       :style               (merge-theme-style :text {})
       :accessibility-label :toast-content}
      middle]]
    (when right right)]])

(defn toast
  [{:keys [icon icon-color text action undo-duration undo-on-press]}]
  [toast-container
   {:left   (when icon
              [icon/icon icon
               {:container-style {:width 20 :height 20}
                :color           (or icon-color
                                     (get-in themes [:icon (theme/get-theme) :color]))}])
    :middle text
    :right  (if undo-duration
              [toast-undo-action undo-duration undo-on-press]
              action)}])
