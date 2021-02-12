(ns status-im.ui.components.list.views
  (:require [reagent.core :as reagent]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.list.styles :as styles]
            [status-im.ui.components.radio :as radio]
            [status-im.ui.components.react :as react]
            [status-im.utils.platform :as platform]
            ["react-native" :as react-native]))

(def flat-list-class (reagent/adapt-react-class (.-FlatList react-native)))
(def section-list-class (reagent/adapt-react-class (.-SectionList react-native)))

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
   [icons/icon icon (merge icon-opts {:style styles/item-icon})]])
;;TODO DEPRECATED, use status-im.ui.components.list-item.views
(defn item-image
  [{:keys [source style image-style]}]
  [react/view {:style style}
   [react/image {:source (if (fn? source) (source) source)
                 :style  (merge styles/item-image image-style)}]])
;;TODO DEPRECATED, use status-im.ui.components.list-item.views
(defn item-primary-only
  ([s] (item-primary-only nil s))
  ([{:keys [style] :as props} s]
   [react/text (merge {:style (merge styles/primary-text-only style)}
                      (dissoc props :style))
    s]))
;;TODO DEPRECATED, use status-im.ui.components.list-item.views
(defn item-content
  [& children]
  (into [react/view {:style styles/item-content-view}] (keep identity children)))

;;TODO DEPRECATED, use status-im.ui.components.list-item.views
(defn list-item-with-radio-button
  [{:keys [on-value-change style checked?] :as props} item]
  [react/touchable-highlight {:on-press #(on-value-change (not checked?))}
   (conj item
         [react/view {:style (merge style styles/item-checkbox)}
          [radio/radio (:checked? props)]])])

(defn- wrap-render-fn [f render-data]
  (fn [^js data]
    (reagent/as-element [f (.-item data) (.-index data) (.-separators data) render-data])))

(defn- wrap-key-fn [f]
  (fn [data index]
    {:post [(some? %)]}
    (f data index)))

(def base-separator [react/view styles/base-separator])

(def default-separator [react/view styles/separator])

(defn- base-list-props
  [{:keys [key-fn render-fn empty-component header footer separator default-separator? render-data]}]
  (let [separator (or separator (when (and platform/ios? default-separator?) default-separator))]
    (merge (when key-fn            {:keyExtractor (wrap-key-fn key-fn)})
           (when render-fn         {:renderItem (wrap-render-fn render-fn render-data)})
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

(defn- wrap-render-section-header-fn [f]
  (fn [^js data]
    (let [^js section (.-section data)]
      (reagent/as-element [f {:title (.-title section)
                              :data  (.-data section)}]))))

(defn- default-render-section-header [{:keys [title data]}]
  (when (seq data)
    [react/view styles/section-header-container
     [react/text {:style styles/section-header}
      title]]))

(defn- wrap-per-section-render-fn [props]
  (update
   (if-let [f (:render-fn props)]
     (assoc (dissoc props :render-fn :render-data) :renderItem (wrap-render-fn f (:render-data props)))
     props)
   :data to-array))

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
