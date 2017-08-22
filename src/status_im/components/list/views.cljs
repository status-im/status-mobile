(ns status-im.components.list.views
  (:require [reagent.core :as r]
            [status-im.components.list.styles :as lst]
            [status-im.components.react :as rn]
            [status-im.utils.platform :as p]))

(def flat-list-class (rn/get-class "FlatList"))
(def section-list-class (rn/get-class "SectionList"))

(defn- wrap-render-fn [f]
  (fn [data]
    ;; For details on passed data
    ;; https://facebook.github.io/react-native/docs/sectionlist.html#rendersectionheader
    (let [{:keys [item index separators]} (js->clj data :keywordize-keys true)]
      (r/as-element (f item index separators)))))

(defn- separator []
  [rn/view lst/separator])

(defn- section-separator []
  [rn/view lst/section-separator])

(defn base-list-props [render-fn empty-component]
  (merge {:renderItem   (wrap-render-fn render-fn)
          :keyExtractor (fn [_ i] i)}
         (when p/ios? {:ItemSeparatorComponent (fn [] (r/as-element [separator]))})
         ; TODO(jeluard) Does not work with our current ReactNative version
         (when empty-component {:ListEmptyComponent (r/as-element [empty-component])})))

(defn flat-list
  "A wrapper for FlatList.
   See https://facebook.github.io/react-native/docs/flatlist.html"
  ([data render-fn] (flat-list data render-fn {}))
  ([data render-fn {:keys [empty-component] :as props}]
   (if (and (empty? data) empty-component)
     ;; TODO(jeluard) remove when native :ListEmptyComponent is supported
     empty-component
     [flat-list-class
      (merge (base-list-props render-fn empty-component)
             {:data (clj->js data)}
             props)])))

(defn- wrap-render-section-header-fn [f]
  (fn [data]
    ;; For details on passed data
    ;; https://facebook.github.io/react-native/docs/sectionlist.html#rendersectionheader
    (let [{:keys [isection]} (js->clj data :keywordize-keys true)]
      (r/as-element (f section)))))

(defn section-list
  "A wrapper for SectionList.
   See https://facebook.github.io/react-native/docs/sectionlist.html"
  ([data render-fn render-section-header-fn] (section-list data render-fn render-section-header-fn {}))
  ([data render-fn render-section-header-fn {:keys [empty-component] :as props}]
   (if (and (empty? data) empty-component)
     empty-component
     [section-list-class
      (merge (base-list-props render-fn empty-component)
             {:sections            (clj->js data)
              :renderSectionHeader (wrap-render-section-header-fn render-section-header-fn)}
             (when p/ios? {:SectionSeparatorComponent (fn [] (r/as-element [section-separator]))})
             props)])))