(ns status-im.ui.components.list-item.views
  (:require [reagent.core :as reagent]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.list-item.styles :as styles]
            [status-im.ui.components.tooltip.views :as tooltip]
            [status-im.ui.screens.chat.photos :as photos]
            [status-im.ui.screens.profile.db :as profile.db]
            [status-im.utils.label :as utils.label]))

(def divider
  [react/view {:height 1 :background-color colors/gray-lighter}])

(defn- icon-column [icon theme disabled?]
  [react/view styles/icon-column-container
   (cond
     (vector? icon)
     icon

     (and (qualified-keyword? icon)
          (= "main-icons" (namespace icon)))
     (let [colors
           (if disabled?
             {:container colors/gray-lighter
              :icon      colors/gray-transparent-40}
             (if (= theme :action-destructive)
               {:container colors/red-light
                :icon      colors/red}
               {:container nil
                :icon      nil}))]
       [react/view (styles/icon-container (:container colors))
        [icons/icon icon (styles/icon (:icon colors))]])

     (and (string? icon)
          (profile.db/base64-encoded-image-path? icon))
     [photos/photo icon {:size 40}]

     :else [icon])])

(defn- title-row [{:keys [title title-color-override title-prefix
                          title-prefix-width title-prefix-height
                          title-accessibility-label title-row-accessory]}
                  type icon? disabled? theme subtitle content accessories]
  [react/view styles/title-row-container
   (when title-prefix
     (cond
       (and (qualified-keyword? title-prefix)
            (= "main-icons" (namespace title-prefix)))
       [icons/icon title-prefix
        (merge
         {:color  colors/gray
          :width  16
          :height 16
          :container-style
          (styles/title-prefix-icon-container
           title-prefix-height title-prefix-width)}
         (when title-prefix-width
           {:width title-prefix-width})
         (when title-prefix-height
           {:height title-prefix-height}))]

       (or (string? title-prefix)
           (number? title-prefix)
           (keyword? title-prefix))
       [react/text {:number-of-lines 1
                    :ellipsize-mode  :tail
                    :style
                    (styles/title-prefix-text
                     type theme icon? subtitle content
                     title-prefix-width disabled?)}
        (if title-prefix-width
          (utils.label/stringify title-prefix)
          (if (string? title)
            (str (utils.label/stringify title-prefix) " ")
            (utils.label/stringify title-prefix)))]

       (vector? title-prefix)
       title-prefix

       :else
       [title-prefix]))

   (cond
     (or (string? title) (keyword? title) (number? title))
     [react/text
      (merge
       {:number-of-lines 1
        :ellipsize-mode  :tail
        :style
        (styles/title
         type theme icon? title-prefix subtitle
         content title-row-accessory disabled?
         title-color-override)}
       (when title-accessibility-label
         {:accessibility-label title-accessibility-label}))
      (utils.label/stringify title)]

     (vector? title)
     [react/view {:flex 1}
      title]

     :else
     [react/view {:flex 1}
      [title]])

   (when title-row-accessory
     [react/view styles/title-row-accessory-container title-row-accessory])])

(defn subtitle-row
  [subtitle-row-elements icon? theme]
  (let [subtitle-row-accessory-width (reagent/atom 0)]
    (reagent/create-class
     {:reagent-render
      (fn [{:keys [subtitle subtitle-max-lines subtitle-row-accessory]} icon? theme]
        [react/view styles/subtitle-row-container
         (cond
           (or (string? subtitle) (keyword? subtitle) (number? subtitle))
           [react/text {:style
                        (merge
                         (styles/subtitle
                          icon? (pos? @subtitle-row-accessory-width)))
                        :number-of-lines subtitle-max-lines
                        :ellipsize-mode  :tail}
            (utils.label/stringify subtitle)]

           (vector? subtitle)
           [react/view
            (styles/subtitle icon? (pos? @subtitle-row-accessory-width))
            subtitle]

           :else
           [react/view
            (styles/subtitle icon? (pos? @subtitle-row-accessory-width))
            [subtitle]])

         (when subtitle-row-accessory
           [react/view
            {:style     styles/subtitle-row-accessory-container
             :on-layout #(reset!
                          subtitle-row-accessory-width
                          (-> % .-nativeEvent .-layout .-width))}
            ;; We do this so that the bottom of the component is 12 device px
            ;; from bottom of the touchable container, instead of 10.
            [react/view {:transform [{:translateY -2}]}
             subtitle-row-accessory]])])})))

(defn- title-column [{:keys [title] :as title-row-elements}
                     {:keys [subtitle] :as subtitle-row-elements}
                     type icon? disabled? theme content accessories]
  [react/view (styles/title-column-container accessories)
   (when title
     [title-row
      title-row-elements type icon? disabled?
      theme subtitle content accessories])

   (when (and subtitle (= :default type))
     [subtitle-row subtitle-row-elements icon? theme])

   (when content
     [react/view {:margin-left (if icon? 2 0)}
      (if (vector? content)
        content
        [content])])])

(defn- accessories-column [accessories theme width]
  (let [last-accessory              (peek accessories)
        last-accessory-is-component (and (not (utils.label/stringify last-accessory))
                                         (not= :chevron last-accessory))
        second-last-accessory       (peek (pop accessories))]
    (into
     [react/view styles/accessories-container]
     (for [accessory accessories]
       (when-not (nil? accessory)
         (with-meta
           (cond
             (= :chevron accessory)
             [icons/icon :main-icons/next
              {:container-style {:width           10
                                 :height          16
                                 :align-items     :center
                                 :justify-content :center}
               :resize-mode     :center
               :color           colors/gray-transparent-40}]

             (and (qualified-keyword? accessory)
                  (= "main-icons" (namespace accessory)))
             [icons/icon
              accessory
              {:color        colors/gray-transparent-40
               :container-style
               {:margin-right (if (= accessory last-accessory) 0 12)}}]

             :else
             [react/view (cond-> {:margin-right (if (= accessory last-accessory) 0 16)}
                           ;; `:chevron` container is 10px wide (see up)
                           ;; but the chevron icon itself is 9px aligned in the
                           ;; container to the right - so 1px white-space on left
                           ;; thats 1px + 15px margin, which makes 16px
                           ;; as intended in design spec
                           (= last-accessory :chevron)
                           (assoc :margin-right 15))
              (cond
                (or (string? accessory) (keyword? accessory) (number? accessory))
                [react/text {:style
                             (styles/accessory-text width (= accessory last-accessory))
                             :ellipsize-mode  :middle
                             :number-of-lines 1}
                 (utils.label/stringify accessory)]

                (vector? accessory)
                accessory

                :else nil)])
           {:key (name (gensym "accessory"))}))))))

(defn list-item
  "A general purpose status-react specfic list item component.
  Every key is optional. Use as needed.
  Combination of around 4 related keys are enough for most cases.
  Spec: https://www.figma.com/file/cb4p8AxLtTF3q1L6JYDnKN15/Index?node-id=787%3A1108

  `react-key`
  String - (default generated automatically using `gensym`)
  A react unique key meta data. Usually for homogeneous list-items.
  Usually needed for list-items generated by looping over
  some kind of collection.
  More here https://reactjs.org/docs/lists-and-keys.html#keys

  `type`
  `:default` (default), `:small`, `:section-header`, or `:divider`
  `:section-header`
  Specifying only these is sufficient.
  {:title <Section title> :type :section-header}
  Optionally set `container-margin-top/bottom`
  `:divider`
  A simple common gray divider seen in various screens.
  Simply use:
  {:type :divider} and specify nothing else.
  White-space above and below it can be controlled by
  `container-margin-top/bottom` specified for list-item above/below

  `theme`
  `:default` (default), `:action`, `:action-destructive`,
  or `:selectable`
  `:selectable`
  A theme for list-item groups having radio button accessory.
  Use it together with `selected?` key. See below.

  `container-margin-top`
  `container-margin-bottom`
  Integer - 0 by default
  Usually the first item has top margin, and the last item has bottom margin.

  `icon`
  Any one of keyword representing `:main-icon/icon`, or
  string representing `photo-path` base64 data, or `component`.
  If component make sure to make it 40x40 size.

  `title-prefix`
  Any one of keyword representing an `vector-icon/icon`,
  `:main-icons/tiny-icons`(16x16) are preferred
  In which case it will automatically have 4px `:margin-top`;
  Any other `vector-icon/icon` is also acceptable.
  In which case it is better to specify height.
  Best to keep it <= 20. See related height/width below.
  String, keyword (gets converted to string),
  Number (gets converted to string), or component.

  `title-prefix-width`
  `title-prefix-height`
  Optional width/height for when title/prefix is not a tiny-icon
  i.e. when icon height/height > 16, or when component.
  Do not specify if title-prefix is tiny-icon

  `title`
  Any one of string, keyword representing translated string in the form of
  :t/{translation-key-in-translation-files},
  Keyword(gets converted to string),
  Number(gets converted to string), or
  Component - When component is used best to keep the style similar.
  to `styles/title-row-container` and/or `styles/title`.

  `title-color-override`
  colors/color - only occasionally needed, self-explanatory

  `title-accessibility-label`
  `:accessibility-label` for title text component.
  Sometimes needed for title - e.g. chat-list-item.
  Makes sense since `title` is the key element of a list item.

  `title-row-accessory`
  Component - Especially made for chat list item, but may serve other
  purpose in the unlikely future. Wrapper already has 2px :margin-top.
  Best to keep it <= 18px high.

  `subtitle`
  Any one of string, keyword representing translated string in the form of
  :t/{translation-key-in-translation-files},
  Keyword(gets converted to string),
  Number(gets converted to string), or
  Component - when component is used best to keep the style similar
  to `styles/subtitle-title-row-container` and/or `styles/subtitle`.

  `subtitle-max-lines`
  Integer - 1 by default - self-explanatory

  `subtitle-row-accessory`
  Component
  Made specially for chat-list to hold unread messages counter.

  Content
  component - to replace entire title-column area
  For visual consistancy with other list-items
  Best to keep height <= 40 for `:default` `type`.
  Best to keep height <= 30 for `:small` `type`.
  Best to keep inner element styles similar to
  `styles/subtitle-title-row-container` and/or `styles/subtitle`.

  `accessories`
  Vector of `:chevron`, Any one of keyword representing `:main-icon/icon`, 
  `number`, `keyword` or `component`
  Long stringified accessory has max-width of 62% of device width.
  That means `title` is also constrained to not be longer than
  30ish%(considering hard right-margin in `title` of 16px)
  In cases of edge cases where title/accessories are
  butting against each other, use component for textual accessories
  with `title` as component as well as necessary.
  Use best judgement with respect to smaller width screens.

  `on-press/on-long-press`
  Function - self explanatory

  `error`
  String - error tooltip

  `accessibility-label`
  :keyword - self explanatory

  `disabled?`
  Boolean - false by default - self explanatory

  `selected?`
  Boolean
  When (= :theme :selectable) this switch controls whether the
  list-item is in a visually selected state. Background-color
  of list-item is colors/gray-selected. Useful for selectable
  list-items like list with radio buttons."

  [{:keys
    [react-key type theme container-margin-top container-margin-bottom
     icon title-prefix title-prefix-width title-prefix-height
     title title-color-override title-row-accessory
     title-accessibility-label subtitle subtitle-max-lines
     subtitle-row-accessory content accessories on-press
     on-long-press error accessibility-label disabled? selected?]
    :or {react-key               (name (gensym "list-item"))
         type                    :default
         theme                   :default
         disabled?               false
         container-margin-top    0
         container-margin-bottom 0
         subtitle-max-lines      1}}]
  (let [title-row-elements
        {:title                     title
         :title-color-override      title-color-override
         :title-accessibility-label title-accessibility-label
         :title-prefix              title-prefix
         :title-prefix-width        title-prefix-width
         :title-prefix-height       title-prefix-height
         :title-row-accessory       title-row-accessory}
        subtitle-row-elements
        {:subtitle               subtitle
         :subtitle-max-lines     subtitle-max-lines
         :subtitle-row-accessory subtitle-row-accessory}
        width           (reagent/atom 0)
        radio-selected? (and (= theme :selectable) selected?)]
    (reagent/create-class
     {:reagent-render
      (fn
        [{:keys
          [icon title-prefix title title-row-accessory subtitle
           subtitle-max-lines subtitle-row-accessory content
           accessories on-press on-long-press error disabled? selected?]
          :or {subtitle-max-lines 1}}]
        (let [title-row-elements
              (merge title-row-elements
                     {:title               title
                      :title-prefix        title-prefix
                      :title-row-accessory title-row-accessory})
              subtitle-row-elements
              {:subtitle               subtitle
               :subtitle-max-lines     subtitle-max-lines
               :subtitle-row-accessory subtitle-row-accessory}
              radio-selected?
              (and (= theme :selectable) selected?)]
          ^{:key react-key}
          (if (= type :divider)
            divider
            [react/view {:style     {:margin-top    container-margin-top
                                     :margin-bottom container-margin-bottom}
                         :on-layout #(reset! width (-> % .-nativeEvent .-layout .-width))}
             [react/touchable-highlight
              (cond-> {:on-press       (when (not= theme :selectable) on-press)
                       :on-press-in    (when (= theme :selectable) on-press)
                       :on-long-press  on-long-press
                       :underlay-color colors/gray-transparent-40
                       :active-opacity (if (= theme :selectable) 1 0.85)
                       :disabled       (or (not on-press) selected?  disabled?)}
                accessibility-label
                (assoc :accessibility-label accessibility-label))
              [react/view {:style (styles/container type radio-selected?)}
               (when icon
                 [icon-column icon theme disabled?])

               (when (or title subtitle content)
                 [title-column
                  title-row-elements subtitle-row-elements
                  type icon disabled? theme content accessories])

               (when accessories
                 [accessories-column accessories theme width])]]
             (when error
               [tooltip/tooltip error styles/error])])))})))
