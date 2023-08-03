(ns react-native.section-list
  (:require ["react-native" :as react-native]
            [react-native.flat-list :as flat-list]
            [reagent.core :as reagent]))

(def section-list-class (reagent/adapt-react-class (.-SectionList react-native)))

(def memo-wrap-render-fn
  (memoize
   (fn [f render-data]
     (fn [^js data]
       (reagent/as-element [f (.-item data) (.-index data) (.-separators data) render-data])))))

(defn- wrap-render-section-header-fn
  [f]
  (fn [^js data]
    (let [^js section (.-section data)]
      (reagent/as-element [f
                           {:title (.-title section)
                            :data  (.-data section)}]))))

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
   To render something on empty sections, use renderSectionFooter and conditionally
   render on empty data
   See https://facebook.github.io/react-native/docs/sectionlist.html"
  [{:keys [sections render-section-header-fn render-section-footer-fn style] :as props}]
  [section-list-class
   (merge (flat-list/base-list-props props)
          props
          (when render-section-footer-fn
            {:renderSectionFooter (wrap-render-section-header-fn render-section-footer-fn)})
          {:sections            (clj->js (map wrap-per-section-render-fn sections))
           :renderSectionHeader (wrap-render-section-header-fn render-section-header-fn)
           :style               style})])
