(ns quo.components.text-combinations.page-top.view
  (:require [clojure.string :as string]
            [quo.components.avatars.channel-avatar.view :as channel-avatar]
            [quo.components.avatars.collection-avatar.view :as collection-avatar]
            [quo.components.avatars.group-avatar.view :as group-avatar]
            [quo.components.inputs.address-input.view :as address-input]
            [quo.components.inputs.recovery-phrase.view :as recovery-phrase]
            [quo.components.inputs.search-input.view :as search-input]
            [quo.components.markdown.text :as text]
            [quo.components.tags.context-tag.view :as context-tag]
            [quo.components.text-combinations.page-top.style :as style]
            [quo.components.text-combinations.standard-title.view :as standard-title]
            [quo.theme]
            [react-native.core :as rn]
            [react-native.fast-image :as fast-image]
            [utils.number]))

(defn- format-counter
  [n]
  (let [num (utils.number/parse-int n)]
    (if (<= num 9)
      (str "0" num)
      (str num))))

(defn- header-counter
  [counter-top counter-bottom]
  [rn/view {:style style/header-counter}
   [text/text
    {:style  style/header-counter-text
     :weight :regular
     :size   :paragraph-2}
    (str (format-counter counter-top)
         "/"
         (format-counter counter-bottom))]])

(defn- header
  [{:keys        [title title-accessibility-label input counter-top counter-bottom
                  title-right title-right-props]
    avatar-props :avatar}]
  (let [title-props (assoc title-right-props
                           :title               title
                           :right               title-right
                           :accessibility-label title-accessibility-label)]
    [rn/view {:style style/header}
     [rn/view {:style style/header-title}
      (when avatar-props
        (let [avatar-props (assoc avatar-props :size :size-32)]
          (if (:group? avatar-props)
            [group-avatar/view avatar-props]
            [channel-avatar/view avatar-props])))
      [standard-title/view title-props]]
     (when (= input :recovery-phrase)
       [header-counter counter-top counter-bottom])]))

(defn- summary-description
  [{:keys [row-1 row-2] :as _summary-props} blur?]
  (let [text-props {:size   :paragraph-2
                    :weight :medium}]
    [rn/view {:style style/summary-description}
     (when-let [{:keys [text-1 text-2 context-tag-1 context-tag-2]} row-1]
       [rn/view {:style style/summary-description-row}
        [text/text text-props text-1]
        [context-tag/view (assoc context-tag-1 :size 24 :blur? blur?)]
        [text/text text-props text-2]
        [context-tag/view (assoc context-tag-2 :size 24 :blur? blur?)]])
     (when-let [{:keys [text-1 text-2 context-tag-1 context-tag-2]} row-2]
       [rn/view {:style style/summary-description-row}
        [text/text text-props text-1]
        [context-tag/view (assoc context-tag-1 :size 24 :blur? blur?)]
        [text/text text-props text-2]
        [context-tag/view (assoc context-tag-2 :size 24 :blur? blur?)]])]))

(defn- community-logo
  [image]
  [rn/view {:accessibility-label :community-logo}
   [fast-image/fast-image
    {:source image
     :style  style/community-logo}]
   [rn/view {:style style/community-logo-ring}]])

(defn- description-container
  [{:keys             [description description-text collection-text community-text blur?
                       collection-image community-image description-accessibility-label]
    context-tag-props :context-tag
    summary-props     :summary}]
  [rn/view {:accessibility-label description-accessibility-label}
   (cond
     (and (= description :text) (not (string/blank? description-text)))
     [text/text
      {:weight :regular
       :size   :paragraph-1}
      description-text]

     (and (= description :context-tag) context-tag-props)
     [rn/view {:style style/context-tag-description}
      [context-tag/view (assoc context-tag-props :size 24 :blur? blur?)]]

     (and (= description :summary) summary-props)
     [summary-description summary-props blur?]

     (= description :collection)
     [rn/view {:style style/image-text-description}
      [collection-avatar/view {:image collection-image}]
      [text/text {:weight :semi-bold :size :paragraph-1}
       collection-text]]

     (= description :community)
     [rn/view {:style style/image-text-description}
      [community-logo community-image]
      [text/text {:weight :semi-bold :size :paragraph-1}
       community-text]])])

(defn- emoji-dash
  [emojis]
  (into [rn/view {:style style/emoji-dash}]
        (map (fn [emoji]
               [rn/view {:style style/emoji}
                [rn/text {:adjusts-font-size-to-fit true} emoji]]))
        emojis))

(defn view
  [{:keys  [description title input blur? input-props container-style]
    emojis :emoji-dash
    :as    props}]
  (let [theme (quo.theme/use-theme)]
    [rn/view {:style container-style}
     [rn/view {:style style/top-container}
      (when (or title input)
        [header props])
      (when description
        [description-container props])
      (when emojis
        [emoji-dash emojis])]
     (when input
       [rn/view {:style (style/input-container theme input blur?)}
        (case input
          :search
          [search-input/search-input
           (assoc input-props
                  :container-style style/search-input-container
                  :blur?           blur?)]

          :address
          [address-input/address-input (assoc input-props :blur? blur?)]

          :recovery-phrase
          [recovery-phrase/recovery-phrase-input
           (assoc input-props
                  :container-style style/recovery-phrase-container
                  :blur?           blur?)]
          nil)])]))
