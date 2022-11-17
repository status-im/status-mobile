(ns react-native.section-list
  (:require [reagent.core :as reagent]
<<<<<<< HEAD
=======
            [status-im.ui.components.list.styles :as styles]
            [status-im.ui.components.react :as react]
>>>>>>> ca683e4a1... add SectionList to RN
            [react-native.flat-list :as flat-list]
            ["react-native" :as react-native]))

(def section-list-class (reagent/adapt-react-class (.-SectionList react-native)))

(def memo-wrap-render-fn
  (memoize
   (fn [f render-data]
     (fn [^js data]
       (reagent/as-element [f (.-item data) (.-index data) (.-separators data) render-data])))))

(defn- wrap-render-section-header-fn [f]
  (fn [^js data]
    (let [^js section (.-section data)]
      (reagent/as-element [f {:title (.-title section)
                              :data  (.-data section)}]))))

<<<<<<< HEAD
=======
(defn- default-render-section-header [{:keys [title data]}]
  (when (seq data)
    [react/view styles/section-header-container
     [react/text {:style styles/section-header}
      title]]))

>>>>>>> ca683e4a1... add SectionList to RN
(defn- wrap-per-section-render-fn [props]
  (update
   (if-let [f (:render-fn props)]
     (assoc (dissoc props :render-fn :render-data) :renderItem (memo-wrap-render-fn f (:render-data props)))
     props)
   :data to-array))

(defn section-list
  "A wrapper for SectionList.
   To render something on empty sections, use renderSectionFooter and conditionaly
   render on empty data
   See https://facebook.github.io/react-native/docs/sectionlist.html"
<<<<<<< HEAD
  [{:keys [sections render-section-header-fn render-section-footer-fn style] :as props}]
=======
  [{:keys [sections render-section-header-fn render-section-footer-fn style] :as props
    :or {render-section-header-fn default-render-section-header
         style {}}}]
>>>>>>> ca683e4a1... add SectionList to RN
  [section-list-class
   (merge (flat-list/base-list-props props)
          props
          (when render-section-footer-fn
            {:renderSectionFooter (wrap-render-section-header-fn render-section-footer-fn)})
          {:sections            (clj->js (map wrap-per-section-render-fn sections))
           :renderSectionHeader (wrap-render-section-header-fn render-section-header-fn)
           :style               style})])
