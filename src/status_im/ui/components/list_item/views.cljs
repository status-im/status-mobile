(ns status-im.ui.components.list-item.views
  (:require [clojure.string :as string]
            [reagent.core :as reagent]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.list-item.styles :as styles]
            [status-im.ui.components.tooltip.views :as tooltip]
            [status-im.ui.screens.profile.db :as profile.db]))

(def divider
  [react/view {:height 1 :background-color colors/gray-lighter}])

(defn stringify [keyword-or-number]
  (cond
    (string? keyword-or-number)
    keyword-or-number

    (and (qualified-keyword? keyword-or-number)
         (= "t" (namespace keyword-or-number)))
    (i18n/label keyword-or-number)

    (and (qualified-keyword? keyword-or-number)
         (not= "t" (namespace keyword-or-number)))
    (str (namespace keyword-or-number) "/" (name keyword-or-number))

    (simple-keyword? keyword-or-number)
    (name keyword-or-number)

    (number? keyword-or-number)
    (str keyword-or-number)

    :else nil))

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
             (case theme
               :action-destructive
               {:container colors/red-light
                :icon      colors/red}

               :blue
               {:container colors/white-transparent-10
                :icon      colors/white}

               {:container nil
                :icon      nil}))]
       [react/view (styles/icon-container (:container colors))
        [icons/icon icon (styles/icon (:icon colors))]])

     (and (string? icon)
          (profile.db/base64-encoded-image-path? icon))
     [react/image {:source      {:uri icon}
                   :resize-mode :cover
                   :style       (styles/photo 40)}]

     :else [icon])])

(defn- title-row [{:keys [title title-color-override title-prefix title-prefix-width
                          title-prefix-height title-row-accessory]}
                  type icon? disabled? theme subtitle content accessories]
  [react/view styles/title-row-container
   (when title-prefix
     (cond
       (and (qualified-keyword? title-prefix)
            (= "main-icons" (namespace title-prefix)))
       [icons/icon title-prefix
        (merge
         {:color           colors/gray
          :width           16
          :height          16
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
          (stringify title-prefix)
          (str (stringify title-prefix) " "))]

       (vector? title-prefix)
       title-prefix

       :else
       [title-prefix]))

   (cond
     (or (string? title) (keyword? title) (number? title))
     [react/text {:number-of-lines 1
                  :ellipsize-mode  :tail
                  :style
                  (styles/title
                   type theme icon? title-prefix subtitle
                   content title-row-accessory disabled?
                   title-color-override)}
      (stringify title)]

     (vector? title)
     [react/view {:flex 1}
      title]

     :else
     [react/view {:flex 1}
      [title]])

   (when title-row-accessory
     [react/view styles/title-row-accessory-container title-row-accessory])])

(defn subtitle-row [{:keys [subtitle subtitle-max-lines subtitle-row-accessory]}
                    icon? theme]
  (let [subtitle-row-accessory-width (reagent/atom 0)]
    (reagent/create-class
     {:render
      (fn []
        [react/view styles/subtitle-row-container
         (cond
           (or (string? subtitle) (keyword? subtitle) (number? subtitle))
           [react/text {:style
                        (merge
                         (styles/subtitle
                          icon? theme (pos? @subtitle-row-accessory-width)))
                        :number-of-lines subtitle-max-lines
                        :ellipsize-mode  :tail}
            (stringify subtitle)]

           (vector? subtitle)
           [react/view
            (styles/subtitle icon? theme (pos? @subtitle-row-accessory-width))
            subtitle]

           :else
           [react/view
            (styles/subtitle icon? theme (pos? @subtitle-row-accessory-width))
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

(defn- accessories-column [accessories theme]
  (let [last-accessory              (peek accessories)
        last-accessory-is-component (and (not (stringify last-accessory))
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
               :color           (if (= theme :blue)
                                  (colors/alpha colors/white 0.4)
                                  colors/gray-transparent-40)}]

             (= :check accessory)
             [icons/icon :main-icons/check
              {:color (if (= theme :blue)
                        (colors/alpha colors/white 0.4)
                        colors/gray)}]

             (= :more accessory)
             [icons/icon :main-icons/more
              {:color (if (= theme :blue)
                        (colors/alpha colors/white 0.4)
                        colors/black)}]

             :else
             [react/view (cond-> {:margin-right (if (= accessory last-accessory) 0 16)}
                           ;; `:chevron` container is 10px wide (see up)
                           ;; but the chevron icon itself is 9px aligned in the
                           ;; container to the right - so 1px white-space on left
                           ;; that 1px + 15px margin makes 16px
                           ;; as intended in design spec
                           (= last-accessory :chevron)
                           (assoc :margin-right 15))
              (cond
                (or (string? accessory) (keyword? accessory) (number? accessory))
                [react/text {:style           (styles/accessory-text theme)
                             :number-of-lines 1}
                 (stringify accessory)]

                (vector? accessory)
                accessory

                :else nil)])
           {:key accessory}))))))

;; every key is optional - use as needed
;; combination of around 4 related keys are enough for most cases

;; type
;; :default (default), :small, or :section-header
;; - :section-header
;;   specifying only these is sufficient
;;   {:title "Section title" :type :section-header}
;;   optionally set `container-margin-top/bottom`

;; theme
;; :default(default), :blue, :action, :action-destructive

;; container-margin-top
;; container-margin-bottom
;; number - 0 by default
;; usually the first item has top margin
;; the last item has bottom margin

;; icon
;; any one of keyword representing :main-icon/icon, photo-path, component
;; if component make sure to make it 40x40 size

;; title-prefix
;; any one of keyword representing an vector-icon/icon,
;; :main-icons/tiny-icons(16x16) are preferred(when it has 4px :margin-top)
;; any other vector-icon/icon is accepted though(when it is better to
;; specify height(best to keep it <= 20) see related height/width below
;; string, keyword(gets converted to string),
;; number(gets converted to string), or component

;; title-prefix-width
;; title-prefix-height
;; optional width/height for when title/prefix is not a tiny-icon
;; i.e. when icon height/height > 16, or when component
;; do not specify if title-prefix is tiny-icon

;; title
;; any one of string, keyword representing translated string in the form of
;; :t/{translation-key-in-translation-files},
;; keyword(gets converted to string),
;; number(gets converted to string), or
;; component - when component is used best to keep the style similar
;; to `styles/title-row-container` and/or `styles/title`

;; title-color-override
;; colors/color - only occasionally needed, self-explanatory

;; title-row-accessory
;; component - especially made for chat list item, but may serve other
;; purpose in the unlikely future. Wrapper already has 2px :margin-top
;; best to keep it <= 18px high

;; subtitle
;; any one of string, keyword representing translated string in the form of
;; :t/{translation-key-in-translation-files},
;; keyword(gets converted to string),
;; number(gets converted to string), or
;; component - when component is used best to keep the style similar
;; to `styles/subtitle-title-row-container` and/or `styles/subtitle`

;; subtitle-max-lines
;; integer - 1 by default - self-explanatory

;; subtitle-row-accessory
;; component
;; made specially for chat-list to hold unread messages counter

;; content
;; component - to replace entire title-column area
;; TODO - consider removing, as it is redundant now that
;; title/subtitle elements can be component as well
;; just make sure to keep in mind the note made on
;; component case on those keys above

;; accessories
;; vector of :chevron, :check, :more, string, number, keyword or component

;; on-press/on-long-press
;; function - self explanatory

;; error
;; string - error tooltip

;; accessibility-label
;; :keyword - self explanatory

;; disabled?
;; boolean - false by default - self explanatory

(defn list-item
  [{:keys [type theme container-margin-top container-margin-bottom
           icon title-prefix title-prefix-width title-prefix-height
           title title-color-override title-row-accessory
           subtitle subtitle-max-lines subtitle-row-accessory
           content accessories on-press on-long-press
           error accessibility-label disabled?]
    :or   {type                    :default
           theme                   :default
           disabled?               false
           container-margin-top    0
           container-margin-bottom 0
           subtitle-max-lines      1}}]
  (let [title-row-elements
        {:title                title
         :title-color-override title-color-override
         :title-prefix         title-prefix
         :title-prefix-width   title-prefix-width
         :title-prefix-height  title-prefix-height
         :title-row-accessory  title-row-accessory}
        subtitle-row-elements
        {:subtitle               subtitle
         :subtitle-max-lines     subtitle-max-lines
         :subtitle-row-accessory subtitle-row-accessory}]
    [react/view {:margin-top    container-margin-top
                 :margin-bottom container-margin-bottom}
     [react/touchable-highlight
      (cond-> {:on-press       on-press
               :on-long-press  on-long-press
               :underlay-color colors/black
               :disabled       (or (not on-press) disabled?)}
        accessibility-label
        (assoc :accessibility-label accessibility-label))
      [react/view {:style (styles/container type theme)}
       (when icon
         [icon-column icon theme disabled?])

       (when (or title subtitle content)
         [title-column
          title-row-elements subtitle-row-elements
          type icon disabled? theme content accessories])

       (when accessories
         [accessories-column accessories theme])]]
     (when error
       [tooltip/tooltip error styles/error])]))
