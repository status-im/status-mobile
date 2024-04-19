(ns quo.components.inputs.search-input.view
  (:require
    [oops.core :as oops]
    [quo.components.icon :as icon]
    [quo.components.inputs.search-input.style :as style]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]))

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
  [{:keys [value tags disabled? blur? on-change-text customization-color
           on-clear on-focus on-blur override-theme container-style]
    :or   {customization-color :blue}
    :as   props}
   & children]
  (let [[state set-state]  (rn/use-state :default)
        on-focus           (rn/use-callback
                            (fn []
                              (set-state :active)
                              (when on-focus (on-focus))))
        on-blur            (rn/use-callback
                            (fn []
                              (set-state :default)
                              (when on-blur (on-blur))))
        scroll-view-ref    (rn/use-ref-atom nil)
        on-scroll-view-ref (rn/use-callback #(reset! scroll-view-ref %))
        on-key-press       (rn/use-callback #(handle-backspace % @scroll-view-ref))
        use-value?         (boolean value)
        clean-props        (apply dissoc props props-to-remove)]
    [rn/view
     {:accessibility-label :search-input
      :style               (style/container container-style)}
     [rn/scroll-view
      {:ref                               on-scroll-view-ref
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
                 :placeholder-text-color (style/placeholder-color state blur? override-theme)
                 :editable               (not disabled?)
                 :on-key-press           on-key-press
                 :keyboard-appearance    (colors/theme-colors :light :dark override-theme)
                 :on-change-text         on-change-text
                 :on-focus               on-focus
                 :on-blur                on-blur}
          use-value?        (assoc :value value)
          (seq clean-props) (merge clean-props))]
       (when-not use-value? children))]
     (when (or (seq value) (seq children))
       [clear-button
        {:on-press       on-clear
         :blur?          blur?
         :override-theme override-theme}])]))
