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

  [section-list {:sections [{:title :key :unik :data {:title  \"\" :subtitle \"\"}}] :render-fn render}]

  or with a per-section `render-fn`

  [section-list {:sections [{:title \"\" :key :unik :render-fn render :data {:title  \"\" :subtitle \"\"}}]}]
  "
  (:require-macros [status-im.utils.views :as views])
  (:require [reagent.core :as reagent]
            [clojure.string :as string]
            [status-im.i18n :as i18n]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.checkbox.view :as checkbox]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.list.styles :as styles]
            [status-im.ui.components.react :as react]
            [status-im.utils.platform :as platform]
            [status-im.ui.screens.home.animations.responder :as responder]))

(def flat-list-class (react/get-class "FlatList"))
(def section-list-class (react/get-class "SectionList"))

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

(defn touchable-item [handler item]
  [react/touchable-highlight {:on-press handler}
   [react/view
    item]])

(defn item-icon
  [{:keys [icon style icon-opts]}]
  {:pre [(not (nil? icon))]}
  [react/view {:style (merge styles/item-icon-wrapper style)}
   [vector-icons/icon icon (merge icon-opts {:style styles/item-icon})]])

(defn item-image
  [{:keys [source style image-style]}]
  [react/view {:style style}
   [react/image {:source source
                 :style  (merge styles/item-image image-style)}]])

(defn item-primary
  ([s] (item-primary nil s))
  ([{:keys [style] :as props} s]
   [react/text (merge {:style styles/primary-text}
                      (dissoc props :style))
    s]))

(defn item-primary-only
  ([s] (item-primary-only nil s))
  ([{:keys [style] :as props} s]
   [react/text (merge {:style (merge styles/primary-text-only style)}
                      (dissoc props :style))
    s]))

(defn item-secondary
  [secondary]
  [react/text {:style styles/secondary-text :ellipsize-mode :middle :number-of-lines 1} secondary])

(defn item-content
  [& children]
  (into [react/view {:style styles/item-content-view}] (keep identity children)))

(defn item-checkbox
  [{:keys [style] :as props}]
  [react/view {:style (merge style styles/item-checkbox)}
   [checkbox/checkbox props]])

(defn list-item-with-checkbox [{:keys [on-value-change checked? plain-checkbox?] :as props} item]
  (let [handler  #(on-value-change (not checked?))
        checkbox [(if plain-checkbox? checkbox/plain-checkbox item-checkbox) props]
        item     (conj item checkbox)]
    [touchable-item handler item]))

(def item-icon-forward
  [item-icon {:icon      :main-icons/next
              :style     {:width 12}
              :icon-opts {:color colors/white}}])

(defn big-list-item
  [{:keys [text text-color value action-fn active? destructive? hide-chevron?
           accessory-value text-color
           accessibility-label icon icon-color image-source icon-content]
    :or   {icon-color colors/blue
           text-color colors/black
           value ""
           active? true}}]
  {:pre [(or icon image-source)
         (and action-fn text)
         (or (nil? accessibility-label) (keyword? accessibility-label))]}
  [react/touchable-highlight
   (cond-> {:on-press action-fn
            :accessibility-label accessibility-label
            :disabled (not active?)})
   [react/view styles/settings-item
    (if icon
      [react/view (styles/settings-item-icon icon-color)
       [vector-icons/icon icon {:color icon-color}]]
      [react/image {:source {:uri image-source}
                    :style   styles/big-item-image}])
    [react/text {:style (styles/settings-item-text text-color)
                 :number-of-lines 1}
     text]
    (when accessory-value
      [react/text {:style           styles/settings-item-value
                   :number-of-lines 1
                   :uppercase?      true}
       (str accessory-value)])
    (when-not hide-chevron?
      [vector-icons/icon :main-icons/next {:color (colors/alpha colors/gray 0.4)}])]])

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
  [{:keys [key-fn render-fn empty-component header separator default-separator?]}]
  (let [separator (or separator (when (and platform/ios? default-separator?) default-separator))]
    (merge (when key-fn          {:keyExtractor (wrap-key-fn key-fn)})
           (when render-fn       {:renderItem (wrap-render-fn render-fn)})
           (when separator       {:ItemSeparatorComponent (fn [] (reagent/as-element separator))})
           (when empty-component {:ListEmptyComponent (fn [] (reagent/as-element empty-component))})
           (when header          {:ListHeaderComponent (fn [] (reagent/as-element header))}))))

;; Workaround an issue in reagent that does not consider JS array as JS value
;; This forces clj <-> js serialization and breaks clj semantic
;; See https://github.com/reagent-project/reagent/issues/335

(deftype Item [value]
  IEncodeJS
  (-clj->js [x] (.-value x))
  (-key->js [x] (.-value x))
  IEncodeClojure
  (-js->clj [x _] (.-value x)))

(defn- to-js-array
  "Converts a collection to a JS array (but leave content as is)"
  [coll]
  (let [arr (array)]
    (doseq [x coll]
      (.push arr x))
    arr))

(defn- wrap-data [o]
  (Item. (to-js-array o)))

(defn flat-list
  "A wrapper for FlatList.
   See https://facebook.github.io/react-native/docs/flatlist.html"
  [{:keys [data] :as props}]
  {:pre [(or (nil? data)
             (sequential? data))]}
  [flat-list-class
   (merge (base-list-props props)
          props
          {:data (wrap-data data)})])

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
   :data wrap-data))

(defn section-list
  "A wrapper for SectionList.
   See https://facebook.github.io/react-native/docs/sectionlist.html"
  [{:keys [sections render-section-header-fn] :as props
    :or {render-section-header-fn default-render-section-header}}]
  [section-list-class
   (merge (base-list-props props)
          props
          {:sections            (clj->js (map wrap-per-section-render-fn sections))
           :renderSectionHeader (wrap-render-section-header-fn render-section-header-fn)})])

(defn render-action [{:keys [label accessibility-label icon action disabled?]}
                     {:keys [action-style action-label-style icon-opts]}]
  [react/touchable-highlight {:on-press action}
   [react/view {:accessibility-label accessibility-label}
    [item
     [item-icon {:icon      icon
                 :style     (merge styles/action
                                   action-style
                                   (when disabled? styles/action-disabled))
                 :icon-opts (merge {:color :white}
                                   icon-opts
                                   (when disabled? {:color colors/gray}))}]
     [item-primary-only {:style (merge styles/action-label
                                       action-label-style
                                       (when disabled? styles/action-label-disabled))}
      label]
     item-icon-forward]]])

(defn action-list [actions {:keys [container-style action-separator-style] :as styles}]
  [react/view (merge styles/action-list container-style)
   [flat-list
    {:separator (when platform/ios?
                  [react/view (merge styles/action-separator
                                     action-separator-style)])
     :data      actions
     :key-fn    (fn [_ i] (str i))
     :render-fn #(render-action % styles)}]])

(defn list-with-label [{:keys [style]} label list]
  [react/view (merge styles/list-with-label-wrapper style)
   [react/text {:style styles/label}
    label]
   list])

(views/defview deletable-list-item [{:keys [type id on-delete]} body]
  (views/letsubs [swiped? [:delete-swipe-position type id]]
    (let [offset-x            (animation/create-value (if swiped? styles/delete-button-width 0))
          swipe-pan-responder (responder/swipe-pan-responder offset-x styles/delete-button-width id swiped?)
          swipe-pan-handler   (responder/pan-handlers swipe-pan-responder)]
      [react/view swipe-pan-handler
       [react/animated-view {:style {:flex 1 :right offset-x}}
        body
        [react/touchable-highlight {:style    styles/delete-icon-highlight
                                    :on-press on-delete}
         [react/view {:style styles/delete-icon-container}
          [vector-icons/icon :main-icons/delete {:color colors/red}]]]]])))
