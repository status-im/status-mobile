(ns status-im.components.list.views
  "
  Wrapper for react-native list components.

  (defn render [{:keys [title subtitle]}]
    [item
     [item-icon :dots_vertical_white]
     [item-content title subtitle]
     [item-icon :arrow_right_gray]])

  [flat-list {:data [{:title  \"\" :subtitle \"\"}] :render-fn render}]

  [section-list {:sections [{:title :key :unik :data {:title  \"\" :subtitle \"\"}}] :render-fn render}]

  or with a per-section `render-fn`

  [section-list {:sections [{:title \"\" :key :unik :render-fn render :data {:title  \"\" :subtitle \"\"}}]}]
  "
  (:require [reagent.core :as r]
            [status-im.components.list.styles :as lst]
            [status-im.components.react :as rn]
            [status-im.components.icons.vector-icons :as vi]
            [status-im.utils.platform :as p]))

(def flat-list-class (rn/get-class "FlatList"))
(def section-list-class (rn/get-class "SectionList"))

(defn item
  ([content] (item nil content))
  ([left-action content] (item left-action content nil))
  ([left-action content right-action]
   [rn/view {:style lst/item}
    left-action
    content
    right-action]))

(defn touchable-item [handler item]
  [rn/touchable-highlight {:on-press handler}
   item])

(defn item-icon [k & [opts]]
  [vi/icon k (merge opts {:style lst/item-image})])

(defn item-image [source]
  [rn/image {:source source
             :style  lst/item-image}])

(defn item-content
  ([primary] (item-content primary nil))
  ([primary secondary] (item-content primary secondary nil))
  ([primary secondary extra]
   [rn/view {:style lst/item-text-view}
    [rn/text {:style (if secondary lst/primary-text lst/primary-text-only)} primary]
    (when secondary
      [rn/text {:style lst/secondary-text :ellipsize-mode "middle" :number-of-lines 1} secondary])
    extra]))

(defn- wrap-render-fn [f]
  (fn [data]
    ;; For details on passed data
    ;; https://facebook.github.io/react-native/docs/sectionlist.html#renderitem
    (let [{:keys [item index separators]} (js->clj data :keywordize-keys true)]
      (r/as-element (f (js->clj item) index separators)))))

(defn- separator []
  [rn/view lst/separator])

(defn- section-separator []
  [rn/view lst/section-separator])

(defn base-list-props [render-fn empty-component]
  (merge {:keyExtractor (fn [_ i] i)}
         (when render-fn {:renderItem (wrap-render-fn render-fn)})
         (when p/ios? {:ItemSeparatorComponent (fn [] (r/as-element [separator]))})
         ; TODO(jeluard) Does not work with our current ReactNative version
         (when empty-component {:ListEmptyComponent (r/as-element [empty-component])})))

(defn flat-list
  "A wrapper for FlatList.
   See https://facebook.github.io/react-native/docs/flatlist.html"
  [{:keys [data render-fn empty-component] :as props}]
  (if (and (empty? data) empty-component)
    ;; TODO(jeluard) remove when native :ListEmptyComponent is supported
    empty-component
    [flat-list-class
     (merge (base-list-props render-fn empty-component)
            {:data (clj->js data)}
            props)]))

(defn- wrap-render-section-header-fn [f]
  (fn [data]
    ;; For details on passed data
    ;; https://facebook.github.io/react-native/docs/sectionlist.html#rendersectionheader
    (let [{:keys [section]} (js->clj data :keywordize-keys true)]
      (r/as-element (f section)))))

(defn- default-render-section-header [m]
  [rn/text {:style lst/section-header} (:title m)])

(defn section-list
  "A wrapper for SectionList.
   See https://facebook.github.io/react-native/docs/sectionlist.html"
  [{:keys [sections render-fn empty-component render-section-header-fn] :or {render-section-header-fn default-render-section-header} :as props}]
  (if (and (empty? sections) empty-component)
    empty-component
    [section-list-class
     (merge (base-list-props render-fn empty-component)
            {:sections            (clj->js (map #(if-let [f (:render-fn %)] (assoc % :renderItem (wrap-render-fn f)) %) sections))
             :renderSectionHeader (wrap-render-section-header-fn render-section-header-fn)}
            (when p/ios? {:SectionSeparatorComponent (fn [] (r/as-element [section-separator]))})
            props)]))