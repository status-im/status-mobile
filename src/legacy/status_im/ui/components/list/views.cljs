(ns legacy.status-im.ui.components.list.views
  (:require
    ["react-native" :as react-native]
    [legacy.status-im.ui.components.list.styles :as styles]
    [legacy.status-im.ui.components.react :as react]
    [react-native.platform :as platform]
    [reagent.core :as reagent]))

(def flat-list-class (reagent/adapt-react-class (.-FlatList react-native)))
(def section-list-class (reagent/adapt-react-class (.-SectionList react-native)))

(def memo-wrap-render-fn
  (memoize
   (fn [f render-data]
     (fn [^js data]
       (reagent/as-element [f (.-item data) (.-index data) (.-separators data) render-data])))))

(def memo-separator-fn
  (memoize
   (fn [separator default-separator?]
     (when-let [separator (or separator
                              (when (and platform/ios? default-separator?)
                                [react/view styles/separator]))]
       (fn []
         (reagent/as-element separator))))))

(def memo-as-element
  (memoize
   (fn [element]
     (reagent/as-element element))))

(def memo-wrap-key-fn
  (memoize
   (fn [f]
     (fn [data index]
       {:post [(some? %)]}
       (f data index)))))

(defn- base-list-props
  [{:keys [key-fn render-fn empty-component header footer separator default-separator? render-data]}]
  (merge (when key-fn {:keyExtractor (memo-wrap-key-fn key-fn)})
         (when render-fn {:renderItem (memo-wrap-render-fn render-fn render-data)})
         (when separator {:ItemSeparatorComponent (memo-separator-fn separator default-separator?)})
         (when empty-component {:ListEmptyComponent (memo-as-element empty-component)})
         (when header {:ListHeaderComponent (memo-as-element header)})
         (when footer {:ListFooterComponent (memo-as-element footer)})))

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

(defn- wrap-render-section-header-fn
  [f]
  (fn [^js data]
    (let [^js section (.-section data)]
      (reagent/as-element [f
                           {:title (.-title section)
                            :data  (.-data section)}]))))

(defn- default-render-section-header
  [{:keys [title data]}]
  (when (seq data)
    [react/view styles/section-header-container
     [react/text {:style styles/section-header}
      title]]))

(defn- wrap-per-section-render-fn
  [props]
  (update
   (if-let [f (:render-fn props)]
     (assoc (dissoc props :render-fn :render-data)
            :renderItem
            (memo-wrap-render-fn f (:render-data props)))
     props)
   :data
   to-array))

(defn section-list
  "A wrapper for SectionList.
   To render something on empty sections, use renderSectionFooter and conditionaly
   render on empty data
   See https://facebook.github.io/react-native/docs/sectionlist.html"
  [{:keys [sections render-section-header-fn render-section-footer-fn style]
    :as   props
    :or   {render-section-header-fn default-render-section-header
           style                    {}}}]
  [section-list-class
   (merge (base-list-props props)
          props
          (when render-section-footer-fn
            {:renderSectionFooter (wrap-render-section-header-fn render-section-footer-fn)})
          {:sections            (clj->js (map wrap-per-section-render-fn sections))
           :renderSectionHeader (wrap-render-section-header-fn render-section-header-fn)
           :style               style})])
