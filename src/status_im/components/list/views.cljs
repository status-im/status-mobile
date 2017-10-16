(ns status-im.components.list.views
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
    [rn/view {:style lst/left-item-wrapper}
     left-action]
    [rn/view {:style lst/content-item-wrapper}
     content]
    (when right-action
      [rn/view {:style lst/right-item-wrapper}
       right-action])]))

(defn touchable-item [handler item]
  [rn/touchable-highlight {:on-press handler}
   item])

(defn item-icon
  [{:keys [icon style icon-opts]}]
  [rn/view {:style style}
   [vi/icon icon (merge icon-opts {:style lst/item-icon})]])

(defn item-image
  ([source] (item-image source nil))
  ([source style]
   [rn/view {:style style}
    [rn/image {:source source
               :style  lst/item-image}]]))

(defn item-primary
  [primary]
  [rn/text {:style lst/primary-text} primary])

(defn item-primary-only
  [primary]
  [rn/text {:style lst/primary-text-only} primary])

(defn item-secondary
  [secondary]
  [rn/text {:style lst/secondary-text :ellipsize-mode "middle" :number-of-lines 1} secondary])

(defn item-content
  [& children]
  (into [rn/view {:style lst/item-text-view}] (keep identity children)))

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

(defn- base-list-props [render-fn empty-component separator?]
  (merge {:keyExtractor (fn [_ i] i)}
         (when render-fn               {:renderItem (wrap-render-fn render-fn)})
         (when (and p/ios? separator?) {:ItemSeparatorComponent (fn [] (r/as-element [separator]))})
         ;; TODO(jeluard) Does not work with our current ReactNative version
         (when empty-component         {:ListEmptyComponent (r/as-element [empty-component])})))

(defn flat-list
  "A wrapper for FlatList.
   See https://facebook.github.io/react-native/docs/flatlist.html"
  [{:keys [data render-fn empty-component separator?] :as props
    :or {separator? true}}]
  {:pre [(or (nil? data)
             (sequential? data))]}
  (if (and (empty? data) empty-component)
    ;; TODO(jeluard) remove when native :ListEmptyComponent is supported
    empty-component
    [flat-list-class
     (merge (base-list-props render-fn empty-component separator?)
            {:data (clj->js data)}
            props)]))

(defn- wrap-render-section-header-fn [f]
  (fn [data]
    ;; For details on passed data
    ;; https://facebook.github.io/react-native/docs/sectionlist.html#rendersectionheader
    (let [{:keys [section]} (js->clj data :keywordize-keys true)]
      (r/as-element (f section)))))

(defn- default-render-section-header [{:keys [title]}]
  [rn/text {:style lst/section-header}
   title])

(defn- wrap-per-section-render-fn [props]
  ;; TODO(jeluard) Somehow wrapping `:render-fn` does not work
  (if-let [f (:render-fn props)]
    (assoc (dissoc props :render-fn) :renderItem (wrap-render-fn f))
    props))

(defn section-list
  "A wrapper for SectionList.
   See https://facebook.github.io/react-native/docs/sectionlist.html"
  [{:keys [sections render-fn empty-component render-section-header-fn separator?] :as props
    :or {render-section-header-fn default-render-section-header
         separator?               true}}]
  (if (and (every? #(empty? (:data %)) sections) empty-component)
    empty-component
    [section-list-class
     (merge (base-list-props render-fn empty-component separator?)
            {:sections            (clj->js (map wrap-per-section-render-fn sections))
             :renderSectionHeader (wrap-render-section-header-fn render-section-header-fn)}
            (when p/ios? {:SectionSeparatorComponent (fn [] (r/as-element [section-separator]))})
            props)]))
