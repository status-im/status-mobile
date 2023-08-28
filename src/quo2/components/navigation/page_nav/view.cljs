(ns quo2.components.navigation.page-nav.view
  (:require [quo2.components.avatars.group-avatar.view :as group-avatar]
            [quo2.components.buttons.button.view :as button]
            [quo2.components.dropdowns.dropdown :as dropdown]
            [quo2.components.icon :as icons]
            [quo2.components.markdown.text :as text]
            [quo2.components.navigation.page-nav.style :as style]
            [quo2.theme :as theme]
            [react-native.core :as rn]))

(def ^:private button-type
  {:white       :grey
   :neutral-5   :dark-grey
   :neutral-90  :grey
   :neutral-95  :dark-grey
   :neutral-100 :black
   :photo       :grey
   :blur        :grey})

(defn- page-nav-base
  [{:keys [margin-top background on-press accessibility-label icon-name]
    :or   {background :white}}
   & children]
  (into [rn/view {:style (style/container margin-top)}
         (when icon-name
           [button/button
            {:type                (button-type background)
             :icon-only?          true
             :size                32
             :on-press            on-press
             :accessibility-label accessibility-label
             :background          (when (#{:photo :blur} background) background)}
            icon-name])]
        children))

(defn- right-section-spacing [] [rn/view {:style style/right-actions-spacing}])

(defn- add-right-buttons-xf
  [max-actions background]
  (comp (filter map?)
        (take max-actions)
        (map (fn [{:keys [icon-name label] :as button-props}]
               [button/button
                (assoc button-props
                       :type       (button-type background)
                       :icon-only? icon-name
                       :size       32
                       :accessible true
                       :background (when (#{:photo :blur} background) background))
                (or label icon-name)]))
        (interpose [right-section-spacing])))

(defn- right-content
  [{:keys [background content max-actions min-size? support-account-switcher?]
    :or   {support-account-switcher? true}}]
  [rn/view (when min-size? {:style style/right-content-min-size})
   (cond
     ;; TODO: use account-switcher when available (issue #16456)
     (and support-account-switcher? (= content :account-switcher))
     [rn/view {:style style/account-switcher-placeholder}]

     (coll? content)
     (into [rn/view {:style style/right-actions-container}]
           (add-right-buttons-xf max-actions background)
           content)

     :else
     nil)])

(defn- title-center
  [{:keys [centered? title]}]
  [rn/view {:style (style/center-content-container centered?)}
   [text/text
    {:weight          :medium
     :size            :paragraph-1
     :number-of-lines 1}
    title]])

(defn- dropdown-center
  [{:keys [theme background dropdown-on-change dropdown-selected? dropdown-text]}]
  (let [dropdown-type (cond
                        (= background :photo)                      :grey
                        (and (= theme :dark) (= background :blur)) :grey
                        :else                                      :ghost)]
    [rn/view {:style (style/center-content-container true)}
     [dropdown/dropdown
      {:type      dropdown-type
       :size      32
       :on-change dropdown-on-change
       :selected  dropdown-selected?}
      dropdown-text]]))

(defn- token-center
  [{:keys [theme background token-logo token-name token-abbreviation]}]
  [rn/view {:style (style/center-content-container false)}
   [rn/image {:style style/token-logo :source token-logo}]
   [text/text
    {:style           style/token-name
     :weight          :semi-bold
     :size            :paragraph-1
     :number-of-lines 1}
    token-name]
   [text/text
    {:style           (style/token-abbreviation theme background)
     :weight          :medium
     :size            :paragraph-2
     :number-of-lines 1}
    token-abbreviation]])

(defn- channel-center
  [{:keys [theme background channel-emoji channel-name channel-icon]}]
  [rn/view {:style (style/center-content-container false)}
   [rn/text {:style style/channel-emoji}
    channel-emoji]
   [text/text
    {:style           style/channel-name
     :weight          :semi-bold
     :size            :paragraph-1
     :number-of-lines 1}
    (str "# " channel-name)]
   [icons/icon channel-icon {:size 16 :color (style/channel-icon-color theme background)}]])

(defn- title-description-center
  [{:keys [background theme picture title description]}]
  [rn/view {:style (style/center-content-container false)}
   (when picture
     [rn/view {:style style/group-avatar-picture}
      [group-avatar/view {:picture picture :size 28}]])
   [rn/view {:style style/title-description-container}
    [text/text
     {:style           style/title-description-title
      :weight          :semi-bold
      :size            :paragraph-1
      :number-of-lines 1}
     title]
    [text/text
     {:style           (style/title-description-description theme background)
      :weight          :medium
      :size            :paragraph-2
      :number-of-lines 1}
     description]]])

(defn- community-network-center
  [{:keys [type community-logo network-logo community-name network-name]}]
  (let [community? (= type :community)
        shown-logo (if community? community-logo network-logo)
        shown-name (if community? community-name network-name)]
    [rn/view {:style (style/center-content-container false)}
     [rn/image
      {:style  style/community-network-logo
       :source shown-logo}]
     [text/text
      {:weight          :semi-bold
       :size            :paragraph-1
       :number-of-lines 1}
      shown-name]]))

(defn- view-internal
  [{:keys [type right-side background text-align]
    :or   {type       :no-title
           text-align :center
           right-side :none
           background :white}
    :as   props}]
  (case type
    :no-title
    [page-nav-base props
     [right-content {:background background :content right-side :max-actions 3}]]

    :title
    (let [centered? (= text-align :center)]
      [page-nav-base props
       [title-center (assoc props :centered? centered?)]
       [right-content
        {:background  background
         :content     right-side
         :max-actions (if centered? 1 3)
         :min-size?   centered?}]])

    :dropdown
    [page-nav-base props
     [dropdown-center props]
     [rn/view {:style style/right-actions-container}
      (let [{button-icon :icon-name :as button-props} (first right-side)]
        [button/button
         (assoc button-props
                :type       (button-type background)
                :icon-only? true
                :size       32
                :accessible true)
         button-icon])]]

    :token
    [page-nav-base props
     [token-center props]
     [right-content {:background background :content right-side :max-actions 3}]]

    :channel
    [page-nav-base props
     [channel-center props]
     [right-content
      {:background                background
       :content                   right-side
       :max-actions               3
       :support-account-switcher? false}]]

    :title-description
    [page-nav-base props
     [title-description-center props]
     [right-content
      {:background                background
       :content                   right-side
       :max-actions               2
       :support-account-switcher? false}]]

    :wallet-networks
    [page-nav-base props
     ;; TODO: use wallet-networks when available (issue #16946)
     [rn/view {:style (style/center-content-container true)}
      [text/text
       {:weight          :regular
        :size            :paragraph-1
        :number-of-lines 1}
       "NETWORK DROPDOWN"]]
     [right-content
      {:background  background
       :content     right-side
       :max-actions 1
       :min-size?   true}]]

    (:community :network)
    [page-nav-base props
     [community-network-center props]
     [right-content
      {:background                background
       :content                   right-side
       :max-actions               3
       :support-account-switcher? false}]]

    nil))

(def page-nav
  "Props:
  - type: defaults to `:no-title`.
  - background:
    `:white`, `:neutral-5`, `:neutral-90`, `:neutral-95`, `:neutral-100`, `:photo` or `:blur`
  - accessibility-label
  - on-press: callback for left button
  - icon-name: icon for left button
  - right-side (optional):
      - The `:account-switcher` keyword
      - vector of maps to render buttons, e.g.:
        {:icon-name           :i/my-icon
         :on-press            (fn callback [] nil)
         :accessibility-label \"an optional label\"}

  Depending on the `type` selected, different properties are accepted:
  `:title`
    - title
    - text-align: `:center` or `:left`
   `:dropdown`
    - dropdown-on-change: a callback
    - dropdown-selected?: a boolean
    - dropdown-text
  `:token`
    - token-logo: a valid rn/image `:source` value
    - token-name: string
    - token-abbreviation: string
  `:channel`
    - channel-emoji: an emoji in a string
    - channel-name
    - channel-icon: an icon keyword (:i/members, :i/lock, etc.)
  `:title-description`
    - title
    - description
    - picture: a valid rn/image `:source` value
  `:wallet-network`
    (Not implemented yet)
  `:community`
    - community-name
    - community-logo: a valid rn/image `:source` value
  `:network`
    - network-name
    - network-logo a valid rn/image `:source` value"
  (theme/with-theme view-internal))
