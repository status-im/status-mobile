(ns quo2.components.notifications.toast
  (:require
   [quo2.components.icon :as icon]
   [quo2.components.markdown.text :as text]
   [quo2.components.notifications.count-down-circle :as count-down-circle]
   [quo2.foundations.colors :as colors]
   [quo2.theme :as theme]
   [react-native.core :as rn]
   [status-im.i18n.i18n :as i18n]))

(def ^:private themes
  {:container        {:light {:background-color colors/neutral-80-opa-80}
                      :dark  {:background-color colors/white-opa-70}}
   :text             {:light {:color colors/white}
                      :dark  {:color colors/neutral-100}}
   :icon             {:light {:color colors/white}
                      :dark  {:color colors/neutral-100}}
   :action-container {:light {:background-color :colors/neutral-80-opa-5}
                      :dark  {:background-color :colors/white-opa-5}}})

(defn- merge-theme-style
  [component-key styles]
  (merge (get-in themes [component-key (theme/get-theme)]) styles))

(defn- toast-left
  [& args]
  (into
   [rn/view
    {:style {:flex-direction :row
             :flex           1
             :justify-content :flex-start
             :align-items    :center
             :width          "100%"}}]
   args))

(defn toast-action-container [{:keys [on-press style]} & children]
  [rn/touchable-highlight
   {:on-press on-press}
   [into
    [rn/view
     {:style    (merge
                 {:flex-direction     :row
                  :padding-vertical   3
                  :padding-horizontal 8
                  :align-items        :center
                  :background-color   (get-in themes [:action-container (theme/get-theme) :background-color])}
                 style)}]
    children]])

(defn toast-undo-acton
  [duration on-press]
  [toast-action-container
   {:on-press on-press}
   [rn/view {:style {:margin-right 5}} [count-down-circle/circle-timer {:duration duration}]]
   [text/text
    {:size   :paragraph-2
     :weight :medium
     :style  (merge-theme-style :text {})}
    [i18n/label :undo]]])

(defn- toast-container
  [{:keys [left middle right]}]
  [rn/view {:style {:padding-left 12 :padding-right 12}}
   [rn/view
    {:style (merge-theme-style :container
                               {:flex-direction  :row
                                :width           "100%"
                                :margin          :auto
                                :justify-content :space-between
                                :align-items     :center
                                :padding         8
                                :border-radius   12})}
    [toast-left
     (when left [rn/view {:style {:padding 4}} left])
     [rn/view {:style {:padding 4 :flex 1}}
      [text/text
       {:size   :paragraph-2
        :weight :medium
        :style  (merge-theme-style :text {})}
       middle]]]

    (when right right)]])

(defn toast
  [{:keys [icon icon-color text action undo-duration undo-on-press]}]
  [toast-container
   {:left   [icon/icon icon
             {:container-style {:width 14 :height 14}
              :color           (or icon-color
                                   (get-in themes [:icon (theme/get-theme) :color]))}]
    :middle text

    :right (if undo-duration [toast-undo-acton undo-duration undo-on-press] action)}])
