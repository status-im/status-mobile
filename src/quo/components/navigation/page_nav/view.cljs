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
    [quo.context]
    [react-native.core :as rn]
    [react-native.reanimated :as reanimated]
    [utils.worklets.profile-header :as header-worklet]))

(def ^:private button-type
  {:white       :grey
   :neutral-5   :dark-grey
   :neutral-90  :grey
   :neutral-95  :dark-grey
   :neutral-100 :black
   :photo       :grey
   :blur        :grey})

(defn- page-nav-base
  [{:keys [margin-top background on-press accessibility-label icon-name behind-overlay? align-center?]
    :or   {background :white}}
   & children]
  (into [rn/view {:style (style/container margin-top)}
         (when icon-name
           [rn/view (when align-center? {:style style/icon-container})
            [button/button
             {:type                (button-type background)
              :icon-only?          true
              :size                32
              :on-press            on-press
              :background          (if behind-overlay?
                                     :blur
                                     (when (button-properties/backgrounds background) background))
              :accessibility-label accessibility-label}
             icon-name]])]
        children))

(defn- right-section-spacing [] [rn/view {:style style/right-actions-spacing}])

(defmulti add-button
  (fn [{:keys [button-props]}]
    (:content-type button-props)))

(defmethod add-button :account-switcher
  [{:keys [support-account-switcher? button-props]}]
  (when support-account-switcher?
    (let [{:keys [customization-color on-press emoji type]} button-props]
      [rn/pressable {:on-press on-press}
       [account-avatar/view
        {:emoji               emoji
         :size                32
         :type                (or type :default)
         :customization-color customization-color}]])))

(defmethod add-button :default
  [{:keys [background behind-overlay? button-props]}]
  (let [{:keys [label icon-name]} button-props]
    [button/button
     (assoc button-props
            :type       (button-type background)
            :icon-only? (boolean icon-name)
            :size       32
            :accessible true
            :background (if behind-overlay?
                          :blur
                          (when (button-properties/backgrounds background) background)))
     (or label icon-name)]))

(defn- add-right-buttons-xf
  [max-actions background behind-overlay? support-account-switcher?]
  (comp (filter map?)
        (take max-actions)
        (map (fn [button-props]
               (add-button {:background                background
                            :behind-overlay?           behind-overlay?
                            :support-account-switcher? support-account-switcher?
                            :button-props              button-props})))
        (interpose [right-section-spacing])))

(defn- right-content
  [{:keys [background content max-actions min-size? support-account-switcher?
           behind-overlay? centered-content?]
    :or   {support-account-switcher? true
           centered-content?         true}}]
  [rn/view (style/right-content min-size? centered-content?)
   (when (coll? content)
     (into [rn/view {:style style/right-actions-container}]
           (add-right-buttons-xf max-actions background behind-overlay? support-account-switcher?)
           content))])

(def header-height 155)
(def page-nav-height 25)
(def threshold (- header-height page-nav-height))

(defn- title-center
  [{:keys [title scroll-y center-content-container-style]}]
  (let [animated-style (when scroll-y
                         (header-worklet/profile-header-animation scroll-y
                                                                  threshold
                                                                  page-nav-height))]
    [reanimated/view
     {:style [center-content-container-style animated-style]}
     [text/text
      {:weight          :medium
       :size            :paragraph-1
       :number-of-lines 1}
      title]]))

(defn- dropdown-center
  [{:keys [background dropdown-on-press dropdown-selected? dropdown-text
           center-content-container-style]}]
  (let [theme          (quo.context/use-theme)
        dropdown-type  (cond
                         (= background :photo)                      :grey
                         (and (= theme :dark) (= background :blur)) :grey
                         :else                                      :ghost)
        dropdown-state (if dropdown-selected? :active :default)]
    [reanimated/view {:style center-content-container-style}
     [dropdown/view
      {:type       dropdown-type
       :state      dropdown-state
       :size       :size-32
       :background (when (dropdown-properties/backgrounds background) background)
       :on-press   dropdown-on-press}
      dropdown-text]]))

(defn- token-center
  [{:keys [background token-logo token-name token-abbreviation center-content-container-style]}]
  (let [theme (quo.context/use-theme)]
    [reanimated/view {:style center-content-container-style}
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
      token-abbreviation]]))

(defn- channel-center
  [{:keys [background channel-emoji channel-name channel-icon center-content-container-style]}]
  (let [theme (quo.context/use-theme)]
    [reanimated/view {:style center-content-container-style}
     [rn/text {:style style/channel-emoji}
      channel-emoji]
     [text/text
      {:style           style/channel-name
       :weight          :semi-bold
       :size            :paragraph-1
       :number-of-lines 1}
      (str "# " channel-name)]
     [icons/icon channel-icon {:size 16 :color (style/channel-icon-color theme background)}]]))

(defn- title-description-center
  [{:keys [background picture title description center-content-container-style]}]
  (let [theme (quo.context/use-theme)]
    [reanimated/view {:style center-content-container-style}
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
       description]]]))

(defn- community-network-center
  [{:keys [type community-logo network-logo community-name network-name center-content-container-style]}]
  (let [community? (= type :community)
        shown-logo (if community? community-logo network-logo)
        shown-name (if community? community-name network-name)]
    [reanimated/view {:style center-content-container-style}
     [rn/image
      {:style  style/community-network-logo
       :source shown-logo}]
     [text/text
      {:weight          :semi-bold
       :size            :paragraph-1
       :number-of-lines 1}
      shown-name]]))

(defn- wallet-networks-center
  [{:keys [networks networks-filtered? networks-on-press show-new-chain-indicator? background
           center-content-container-style]}]
  [reanimated/view {:style center-content-container-style}
   [network-dropdown/view
    {:state                     :default
     :networks-filtered?        networks-filtered?
     :on-press                  networks-on-press
     :blur?                     (= background :blur)
     :show-new-chain-indicator? show-new-chain-indicator?}
    networks]])

(defn page-nav
  "Props:
  - type: defaults to `:no-title`.
  - background:
    `:white`, `:neutral-5`, `:neutral-90`, `:neutral-95`, `:neutral-100`, `:photo` or `:blur`
  - accessibility-label
  - on-press: callback for left button
  - icon-name: icon for left button
  - right-side (optional):
      - vector of maps to render buttons, e.g.:
        {:icon-name           :i/my-icon
         :on-press            (fn callback [] nil)
         :accessibility-label \"an optional label\"}

  Depending on the `type` selected, different properties are accepted:
  `:title`
    - title
    - text-align: `:center` or `:left`
    - scroll-y: a shared value (optional)
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
  `:wallet-networks`
    - networks: a vector of network image source
    - networks-on-press: a callback
  `:community`
    - community-name
    - community-logo: a valid rn/image `:source` value
  `:network`
    - network-name
    - network-logo a valid rn/image `:source` value"
  [{:keys [type right-side background text-align behind-overlay? center-opacity]
    :or   {type       :no-title
           text-align :center
           right-side :none
           background :white}
    :as   props}]
  (let [centered-content?              (case type
                                         :title                       (= text-align :center)
                                         (:dropdown :wallet-networks) true
                                         false)
        center-content-container-style (reanimated/apply-animations-to-style
                                        (if center-opacity
                                          {:opacity center-opacity}
                                          nil)
                                        (style/center-content-container centered-content?))
        props-with-style               (assoc props
                                              :center-content-container-style
                                              center-content-container-style)]
    (case type
      :no-title
      [page-nav-base props
       [right-content
        {:background      background
         :content         right-side
         :max-actions     3
         :behind-overlay? behind-overlay?}]]

      :title
      (let [centered? (= text-align :center)]
        [page-nav-base props
         [title-center props-with-style]
         [right-content
          {:background  background
           :content     right-side
           :max-actions (if centered? 1 3)
           :min-size?   centered?}]])

      :dropdown
      [page-nav-base props
       [dropdown-center props-with-style]
       [right-content
        {:background                background
         :content                   right-side
         :max-actions               1
         :support-account-switcher? false}]]

      :token
      [page-nav-base props
       [token-center props-with-style]
       [right-content
        {:background  background
         :content     right-side
         :max-actions 3}]]

      :channel
      [page-nav-base props
       [channel-center props-with-style]
       [right-content
        {:background                background
         :content                   right-side
         :max-actions               3
         :support-account-switcher? false}]]

      :title-description
      [page-nav-base props
       [title-description-center props-with-style]
       [right-content
        {:background                background
         :content                   right-side
         :max-actions               2
         :support-account-switcher? false}]]

      :wallet-networks
      [page-nav-base props
       [wallet-networks-center props-with-style]
       [right-content
        {:background  background
         :content     right-side
         :max-actions 3
         :min-size?   true}]]

      (:community :network)
      [page-nav-base props
       [community-network-center props-with-style]
       [right-content
        {:background                background
         :content                   right-side
         :max-actions               3
         :support-account-switcher? false
         :centered-content?         centered-content?}]]

      nil)))
