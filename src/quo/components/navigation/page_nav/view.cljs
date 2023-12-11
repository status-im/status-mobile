(ns quo.components.navigation.page-nav.view
  (:require
    [quo.components.avatars.account-avatar.view :as account-avatar]
    [quo.components.avatars.group-avatar.view :as group-avatar]
    [quo.components.buttons.button.properties :as button-properties]
    [quo.components.buttons.button.view :as button]
    [quo.components.dropdowns.dropdown.properties :as dropdown-properties]
    [quo.components.dropdowns.dropdown.view :as dropdown]
    [quo.components.dropdowns.network-dropdown.view :as network-dropdown]
    [quo.components.icon :as icons]
    [quo.components.markdown.text :as text]
    [quo.components.navigation.page-nav.style :as style]
    [quo.theme :as theme]
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
  [{:keys [margin-top background on-press accessibility-label icon-name behind-overlay?]
    :or   {background :white}}
   & children]
  (into [rn/view {:style (style/container margin-top)}
         (when icon-name
           [button/button
            {:type                (button-type background)
             :icon-only?          true
             :size                32
             :on-press            on-press
             :background          (if behind-overlay?
                                    :blur
                                    (button-properties/backgrounds background))
             :accessibility-label accessibility-label}
            icon-name])]
        children))

(defn- right-section-spacing [] [rn/view {:style style/right-actions-spacing}])

(defn- add-right-buttons-xf
  [max-actions background behind-overlay?]
  (comp (filter map?)
        (take max-actions)
        (map (fn [{:keys [icon-name label] :as button-props}]
               [button/button
                (assoc button-props
                       :type       (button-type background)
                       :icon-only? icon-name
                       :size       32
                       :accessible true
                       :background (if behind-overlay?
                                     :blur
                                     (when (button-properties/backgrounds background) background)))
                (or label icon-name)]))
        (interpose [right-section-spacing])))

(defn- account-switcher-content
  [{:keys [customization-color on-press emoji type]}]
  [rn/pressable {:on-press on-press}
   [account-avatar/view
    {:emoji               emoji
     :size                32
     :type                (or type :default)
     :customization-color customization-color}]])

(defn- right-content
  [{:keys [background content max-actions min-size? support-account-switcher? account-switcher
           behind-overlay?]
    :or   {support-account-switcher? true}}]
  [rn/view (when min-size? {:style style/right-content-min-size})
   (cond
     (and support-account-switcher? (= content :account-switcher))
     [account-switcher-content account-switcher]

     (coll? content)
     (into [rn/view {:style style/right-actions-container}]
           (add-right-buttons-xf max-actions background behind-overlay?)
           content)

     :else
     nil)])

(defn- title-center
  [{:keys [centered? title center-opacity]}]
  [rn/view {:style (style/center-content-container centered? center-opacity)}
   [text/text
    {:weight          :medium
     :size            :paragraph-1
     :number-of-lines 1}
    title]])

(defn- dropdown-center
  [{:keys [theme background dropdown-on-press dropdown-selected? dropdown-text center-opacity]}]
  (let [dropdown-type  (cond
                         (= background :photo)                      :grey
                         (and (= theme :dark) (= background :blur)) :grey
                         :else                                      :ghost)
        dropdown-state (if dropdown-selected? :active :default)]
    [rn/view {:style (style/center-content-container true center-opacity)}
     [dropdown/view
      {:type       dropdown-type
       :state      dropdown-state
       :size       :size-32
       :background (when (dropdown-properties/backgrounds background) background)
       :on-press   dropdown-on-press}
      dropdown-text]]))

(defn- token-center
  [{:keys [theme background token-logo token-name token-abbreviation center-opacity]}]
  [rn/view {:style (style/center-content-container false center-opacity)}
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
  [{:keys [theme background channel-emoji channel-name channel-icon center-opacity]}]
  [rn/view {:style (style/center-content-container false center-opacity)}
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
  [{:keys [background theme picture title description center-opacity]}]
  [rn/view {:style (style/center-content-container false center-opacity)}
   (when picture
     [rn/view {:style style/group-avatar-picture}
      [group-avatar/view {:picture picture :size :size-28}]])
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
  [{:keys [type community-logo network-logo community-name network-name center-opacity]}]
  (let [community? (= type :community)
        shown-logo (if community? community-logo network-logo)
        shown-name (if community? community-name network-name)]
    [rn/view {:style (style/center-content-container false center-opacity)}
     [rn/image
      {:style  style/community-network-logo
       :source shown-logo}]
     [text/text
      {:weight          :semi-bold
       :size            :paragraph-1
       :number-of-lines 1}
      shown-name]]))

(defn- wallet-networks-center
  [{:keys [networks networks-on-press background center-opacity]}]
  [rn/view {:style (style/center-content-container true center-opacity)}
   [network-dropdown/view
    {:state    :default
     :on-press networks-on-press
     :blur?    (= background :blur)} networks]])

(defn- view-internal
  "behind-overlay is necessary for us to know if the page-nav buttons are under the bottom sheet overlay or not."
  [{:keys [type right-side background text-align account-switcher behind-overlay?]
    :or   {type       :no-title
           text-align :center
           right-side :none
           background :white}
    :as   props}]
  (case type
    :no-title
    [page-nav-base props
     [right-content
      {:background       background
       :content          right-side
       :max-actions      3
       :behind-overlay?  behind-overlay?
       :account-switcher account-switcher}]]

    :title
    (let [centered? (= text-align :center)]
      [page-nav-base props
       [title-center (assoc props :centered? centered?)]
       [right-content
        {:background       background
         :content          right-side
         :max-actions      (if centered? 1 3)
         :min-size?        centered?
         :account-switcher account-switcher}]])

    :dropdown
    [page-nav-base props
     [dropdown-center props]
     [right-content
      {:background                background
       :content                   right-side
       :max-actions               1
       :support-account-switcher? false}]]

    :token
    [page-nav-base props
     [token-center props]
     [right-content
      {:background       background
       :content          right-side
       :max-actions      3
       :account-switcher account-switcher}]]

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
     [wallet-networks-center props]
     [right-content
      {:background       background
       :content          right-side
       :max-actions      1
       :min-size?        true
       :account-switcher account-switcher}]]

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

  - account-switcher (optional)
      - props to render dropdown component (emoji only) e.g.:
       {:customization-color :purple
        :on-press            (fn [] nil)
        :state               :default (inherit dropdown states)
        :emoji               \"🍑\"}

  Depending on the `type` selected, different properties are accepted:
  `:title`
    - title
    - text-align: `:center` or `:left`
   `:dropdown`
    - dropdown-on-press:  a callback
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
    - networks: a vector of network image source
    - networks-on-press: a callback
  `:community`
    - community-name
    - community-logo: a valid rn/image `:source` value
  `:network`
    - network-name
    - network-logo a valid rn/image `:source` value"
  (theme/with-theme view-internal))
