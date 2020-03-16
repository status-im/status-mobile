(ns status-im.ui.components.list.views
  "
  Wrapper for react-native list components.

  (defn render [{:keys [title subtitle]}]
    [item
     [item-icon {:icon :dots_vertical_white}]
     [item-content
      [item-primary title]
      [item-secondary subtitle]]
     [item-icon {:icon :arrow_right_gray}]])

  [flat-list {:data [{:title  \"\" :subtitle \"\"}] :render-fn render}]

  [section-list {:sections [{:title \"\" :key :unik :data {:title  \"\" :subtitle \"\"}}] :render-fn render}]

  or with a per-section `render-fn`

  [section-list {:sections [{:title \"\" :key :unik :render-fn render :data {:title  \"\" :subtitle \"\"}}]}]
  "
  (:require [clojure.string :as string]
            [reagent.core :as reagent]
            [status-im.i18n :as i18n]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.checkbox.view :as checkbox]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.list-item.views :as list-item]
            [status-im.ui.components.list.styles :as styles]
            [status-im.ui.components.react :as react]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.radio :as radio])
  (:require-macros [status-im.utils.views :as views]))

(def flat-list-class (react/get-class "FlatList"))
(def section-list-class (react/get-class "SectionList"))

;;TODO THIS NAMESPACE is DEPRECATED, use status-im.ui.components.list-item.views
;;TODO DEPRECATED, use status-im.ui.components.list-item.views
(defn item
  ([content] (item nil content))
  ([left content] (item left content nil))
  ([left content right]
   [react/view {:style styles/item}
    (when left
      [react/view {:style styles/left-item-wrapper}
       left])
    [react/view {:style styles/content-item-wrapper}
     content]
    (when right
      [react/view {:style styles/right-item-wrapper}
       right])]))
;;TODO DEPRECATED, use status-im.ui.components.list-item.views
(defn touchable-item [handler item]
  [react/touchable-highlight {:on-press handler}
   [react/view
    item]])
;;TODO DEPRECATED, use status-im.ui.components.list-item.views
(defn item-icon
  [{:keys [icon style icon-opts]}]
  {:pre [(not (nil? icon))]}
  [react/view {:style (merge styles/item-icon-wrapper style)}
   [vector-icons/icon icon (merge icon-opts {:style styles/item-icon})]])
;;TODO DEPRECATED, use status-im.ui.components.list-item.views
(defn item-image
  [{:keys [source style image-style]}]
  [react/view {:style style}
   [react/image {:source (if (fn? source) (source) source)
                 :style  (merge styles/item-image image-style)}]])
;;TODO DEPRECATED, use status-im.ui.components.list-item.views
(defn item-primary
  ([s] (item-primary nil s))
  ([{:keys [style] :as props} s]
   [react/text (merge {:style (merge styles/primary-text style)}
                      (dissoc props :style))
    s]))
;;TODO DEPRECATED, use status-im.ui.components.list-item.views
(defn item-primary-only
  ([s] (item-primary-only nil s))
  ([{:keys [style] :as props} s]
   [react/text (merge {:style (merge styles/primary-text-only style)}
                      (dissoc props :style))
    s]))
;;TODO DEPRECATED, use status-im.ui.components.list-item.views
(defn item-secondary
  ([s] (item-secondary nil s))
  ([{:keys [style]} s]
   [react/text
    {:style           (merge styles/secondary-text style)
     :ellipsize-mode  :middle
     :number-of-lines 1}
    s]))
;;TODO DEPRECATED, use status-im.ui.components.list-item.views
(defn item-content
  [& children]
  (into [react/view {:style styles/item-content-view}] (keep identity children)))

;;TODO DEPRECATED, use status-im.ui.components.list-item.views
(defn list-item-with-checkbox
  [{:keys [on-value-change style checked? on-long-press] :as props} item]
  [react/touchable-highlight (merge {:on-press #(on-value-change (not checked?))}
                                    (when on-long-press
                                      {:on-long-press on-long-press}))
   (conj item
         [react/view {:style (merge style styles/item-checkbox)}
          [checkbox/checkbox (dissoc props :on-value-change)]])])

;;TODO DEPRECATED, use status-im.ui.components.list-item.views
(defn list-item-with-radio-button
  [{:keys [on-value-change style checked?] :as props} item]
  [react/touchable-highlight {:on-press #(on-value-change (not checked?))}
   (conj item
         [react/view {:style (merge style styles/item-checkbox)}
          [radio/radio (:checked? props)]])])

(def item-icon-forward
  [item-icon {:icon      :main-icons/next
              :style     {:width 12}
              :icon-opts {:color colors/white}}])

;;TODO DEPRECATED, use status-im.ui.components.list-item.views
(defn big-list-item
  [{:keys [style text text-color text-style subtext value action-fn active? destructive? hide-chevron?
           accessory-value text-color new? activity-indicator
           accessibility-label icon icon-color image-source icon-content]
    :or   {icon-color colors/blue
           text-color colors/black
           value ""
           active? true
           style {}}}]
  {:pre [text
         (or (nil? accessibility-label) (keyword? accessibility-label))]}
  [react/touchable-highlight
   {:on-press action-fn
    :style style
    :accessibility-label accessibility-label
    :disabled (not active?)}
   [react/view styles/settings-item
    (cond
      icon
      [react/view (styles/settings-item-icon icon-color subtext)
       [vector-icons/icon icon {:color icon-color}]]
      image-source
      [react/image {:source {:uri image-source}
                    :style   styles/big-item-image}]
      activity-indicator
      [react/view (styles/settings-item-icon icon-color subtext)
       [react/activity-indicator activity-indicator]])
    (if subtext
      [react/view {:style styles/settings-item-text-container}
       [react/view {:style styles/settings-item-main-text-container}
        (when new?
          [react/view {:style styles/new-label}
           [react/text {:style styles/new-label-text}
            (string/upper-case (i18n/label :t/new))]])
        [react/text {:style (merge (styles/settings-item-text text-color) text-style)}
         text]]
       [react/view {:style {:margin-top 2
                            :justify-content :flex-start}}
        [react/text {:style styles/settings-item-subtext
                     :number-of-lines 2}
         subtext]]]
      [react/text {:style           (merge (styles/settings-item-text text-color) text-style)
                   :number-of-lines 1}
       text])
    (when accessory-value
      [react/text {:style           styles/settings-item-value
                   :number-of-lines 1}
       (str accessory-value)])
    (when-not hide-chevron?
      [vector-icons/icon :main-icons/next {:color colors/gray-transparent-40}])]])

(defn- wrap-render-fn [f]
  (fn [data]
    (reagent/as-element (f (.-item data) (.-index data) (.-separators data)))))

(defn- wrap-key-fn [f]
  (fn [data index]
    {:post [(some? %)]}
    (f data index)))

(def base-separator [react/view styles/base-separator])

(def default-separator [react/view styles/separator])

(def default-header [react/view styles/list-header-footer-spacing])

(def default-footer [react/view styles/list-header-footer-spacing])

(defn- base-list-props
  [{:keys [key-fn render-fn empty-component header footer separator default-separator?]}]
  (let [separator (or separator (when (and platform/ios? default-separator?) default-separator))]
    (merge (when key-fn            {:keyExtractor (wrap-key-fn key-fn)})
           (when render-fn         {:renderItem (wrap-render-fn render-fn)})
           (when separator         {:ItemSeparatorComponent (fn [] (reagent/as-element separator))})
           (when empty-component   {:ListEmptyComponent (fn [] (reagent/as-element empty-component))})
           ;; header and footer not wrapped in anonymous function to prevent re-creation on every re-render
           ;; More details can be found here - https://github.com/facebook/react-native/issues/13602#issuecomment-300608431
           (when header            {:ListHeaderComponent (reagent/as-element header)})
           (when footer            {:ListFooterComponent (reagent/as-element footer)}))))

(defn flat-list
  "A wrapper for FlatList.
   See https://facebook.github.io/react-native/docs/flatlist.html"
  ([props] (flat-list props nil))
  ([{:keys [data] :as props} {:keys [animated?]}]
   (let [class (if animated? react/animated-flat-list-class flat-list-class)]
     {:pre [(or (nil? data)
                (sequential? data))]}
     [class
      (merge (base-list-props props)
             props
             {:data (to-array data)})])))

(defn flat-list-generic-render-fn
  "A generic status-react specific `render-fn` for `list-item`.
  Handles `list-item` `data` consiting any combination of
  `list-item/list-item` config map and `companent`."
  [item _]
  (cond
    (map? item)    [list-item/list-item item]
    (vector? item) item
    (nil? item)    nil
    :else          [item]))

(defn- wrap-render-section-header-fn [f]
  (fn [data]
    (let [section (.-section data)]
      (reagent/as-element (f {:title (.-title section)
                              :data  (.-data section)})))))

(defn- default-render-section-header [{:keys [title data]}]
  (when (seq data)
    [react/view styles/section-header-container
     [react/text {:style styles/section-header}
      title]]))

(defn- wrap-per-section-render-fn [props]
  (update
   (if-let [f (:render-fn props)]
     (assoc (dissoc props :render-fn) :renderItem (wrap-render-fn f))
     props)
   :data to-array))
;;TODO DEPRECATED, use status-im.ui.components.list-item.views
(defn section-list
  "A wrapper for SectionList.
   To render something on empty sections, use renderSectionFooter and conditionaly
   render on empty data
   See https://facebook.github.io/react-native/docs/sectionlist.html"
  [{:keys [sections render-section-header-fn render-section-footer-fn style] :as props
    :or {render-section-header-fn default-render-section-header
         style {}}}]
  [section-list-class
   (merge (base-list-props props)
          props
          (when render-section-footer-fn
            {:renderSectionFooter (wrap-render-section-header-fn render-section-footer-fn)})
          {:sections            (clj->js (map wrap-per-section-render-fn sections))
           :renderSectionHeader (wrap-render-section-header-fn render-section-header-fn)
           :style               style})])

;;TODO DEPRECATED, use status-im.ui.components.list-item.views
(defn list-with-label [{:keys [style]} label list]
  [react/view (merge styles/list-with-label-wrapper style)
   [react/text {:style styles/label}
    label]
   list])
