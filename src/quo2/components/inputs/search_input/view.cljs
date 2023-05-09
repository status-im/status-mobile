(ns quo2.components.inputs.search-input.view
  (:require [oops.core :as oops]
            [quo2.components.icon :as icon]
            [quo2.components.inputs.search-input.style :as style]
            [react-native.core :as rn]
            [reagent.core :as reagent]))

(def ^:private tag-separator [rn/view {:style style/tag-separator}])

(defn- inner-tags
  [tags-coll]
  (into [rn/view {:style style/tag-container}]
        (interpose tag-separator)
        tags-coll))

(defn- clear-button
  [{:keys [on-press blur? override-theme]}]
  [rn/touchable-opacity
   {:style    style/clear-icon-container
    :on-press on-press}
   [icon/icon :i/clear
    {:color (style/clear-icon blur? override-theme)
     :size  20}]])

(defn- handle-backspace
  [event ^js/Object scroll-view-ref]
  (when (and (= (oops/oget event "nativeEvent.key") "Backspace")
             scroll-view-ref)
    (.scrollToEnd scroll-view-ref #js {:animated false})))

(def ^:private props-to-remove
  [:cursor-color :placeholder-text-color :editable :on-change-text :on-focus
   :on-blur :on-clear :value :tags :disabled? :blur? :customization-color :override-theme])

(defn- add-children
  [text-input children]
  (if (seq children)
    (into text-input children)
    text-input))

(defn search-input
  [{:keys [value]}]
  (let [state           (reagent/atom :default)
        set-active      #(reset! state :active)
        set-default     #(reset! state :default)
        scroll-view-ref (atom nil)
        use-value?      (boolean value)]
    (fn [{:keys [value tags disabled? blur? on-change-text customization-color
                 on-clear on-focus on-blur override-theme]
          :or   {customization-color :blue}
          :as   props}
         & children]
      (let [clean-props (apply dissoc props props-to-remove)]
        [rn/view {:style style/container}
         [rn/scroll-view
          {:ref                               #(reset! scroll-view-ref %)
           :style                             style/scroll-container
           :content-container-style           style/scroll-content
           :horizontal                        true
           :shows-horizontal-scroll-indicator false}
          (when (seq tags)
            [inner-tags tags])

          (add-children
           [rn/text-input
            (cond-> {:style                  (style/input-text disabled?)
                     :cursor-color           (style/cursor customization-color override-theme)
                     :placeholder-text-color (style/placeholder-color @state blur? override-theme)
                     :editable               (not disabled?)
                     :on-key-press           #(handle-backspace % @scroll-view-ref)
                     :on-change-text         (fn [new-text]
                                               (when on-change-text
                                                 (on-change-text new-text))
                                               (reagent/flush))
                     :on-focus               (fn []
                                               (set-active)
                                               (when on-focus (on-focus)))
                     :on-blur                (fn []
                                               (set-default)
                                               (when on-blur (on-blur)))}
              use-value?        (assoc :value value)
              (seq clean-props) (merge clean-props))]
           (when-not use-value? children))]

         (when (or (seq value) (seq children))
           [clear-button
            {:on-press       on-clear
             :blur?          blur?
             :override-theme override-theme}])]))))
