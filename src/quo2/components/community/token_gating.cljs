(ns quo2.components.community.token-gating
  (:require [quo2.components.avatars.channel-avatar :as channel-avatar]
            [quo2.components.buttons.button :as button]
            [quo2.components.icon :as icon]
            [quo2.components.info.information-box :as information-box]
            [quo2.components.markdown.text :as text]
            [quo2.components.tags.token-tag :as token-tag]
            [quo2.foundations.colors :as colors]
            [quo2.i18n :as i18n]
            [react-native.fast-image :as fast-image]
            [react-native.core :as rn]))

(def ^:private token-tag-horizontal-spacing 7)
(def token-tag-vertical-spacing 5)

(def styles
  {:container                      {:flex               1
                                    :padding-horizontal 20}
   :header-container               {:flex-direction :row
                                    :align-items    :center}
   :header-spacing-community       {:margin-bottom 16}
   :header-spacing-channel         {:margin-bottom 24}
   :header-avatar                  {:margin-right 8}
   :header-community-avatar-image  {:width 32 :height 32}
   :header-title-container         {:flex           1
                                    :flex-direction :row
                                    :align-items    :center}
   :header-title-lock              {:margin-left 6}
   :header-info-button             {:margin-left 8}
   :token-requirement-text-spacing {:margin-vertical 8}
   :token-requirement-list-spacing {:padding-vertical (- 8 token-tag-vertical-spacing)}
   :token-row                      {:flex-direction :row
                                    :flex-wrap      :wrap
                                    :align-items    :center}
   :token-row-container            {:flex-direction    :row
                                    :align-items       :center
                                    :margin-horizontal (- token-tag-horizontal-spacing)}
   :token-row-container-no-shift   {:margin-left 0}
   :token-row-or-text              {:margin-right 3}
   :token-tag-spacing              {:margin-horizontal token-tag-horizontal-spacing
                                    :margin-vertical   token-tag-vertical-spacing}
   :divider                        {:height            1
                                    :margin-horizontal -20
                                    :margin-top        (- 12 token-tag-vertical-spacing)
                                    :margin-bottom     8}
   :membership-request-denied      {:margin-top 13}
   :enter-button                   {:margin-top 16}
   :info-text                      {:margin-top         12
                                    :text-align         :center
                                    :padding-horizontal 20}})

(defn multiple-token-requirements?
  [token-requirement-lists]
  (when (vector? (first token-requirement-lists))
    (> (count token-requirement-lists) 1)))

(defn is-token-requirement-met?
  [token-requirement-list]
  (and (seq token-requirement-list)
       (every?
        (fn [token]
          (get token :is-sufficient?))
        token-requirement-list)))

(defn are-multiple-token-requirements-met?
  [token-requirement-lists]
  (if (multiple-token-requirements? token-requirement-lists)
    (some is-token-requirement-met? token-requirement-lists)
    (is-token-requirement-met? token-requirement-lists)))

(defn token-requirement-list-row
  [tokens community-color]
  [rn/view
   {:style (merge
            (get styles :token-row)
            (get styles :token-requirement-list-spacing))}
   (map-indexed (fn [token-index token]
                  (let [{:keys [token-img-src token amount is-sufficient? is-purchasable?]} token]
                    ^{:key token-index}
                    [rn/view {:style (get styles :token-tag-spacing)}
                     [token-tag/token-tag
                      {:token          token
                       :value          amount
                       :size           24
                       :border-color   community-color
                       :is-required    is-sufficient?
                       :is-purchasable is-purchasable?
                       :token-img-src  token-img-src}]]))
                tokens)])

(defn token-requirement-list
  [props community-color]
  (let [{:keys [gate token-requirements-changed?
                required-tokens-lost?]}     props
        [gate-type token-requirement-lists] gate
        multiple-token-requirements?        (multiple-token-requirements? token-requirement-lists)
        is-sufficient?                      (are-multiple-token-requirements-met?
                                             token-requirement-lists)
        you-must-hold-label                 (if (= gate-type :join)
                                              (cond
                                                token-requirements-changed? :t/you-must-now-hold
                                                required-tokens-lost?       :t/you-must-always-hold
                                                :else                       :t/you-must-hold)
                                              :t/you-must-hold)
        message-label                       (cond
                                              (= gate-type :join)
                                              (cond
                                                token-requirements-changed?
                                                :t/community-join-requirements-changed
                                                required-tokens-lost?
                                                :t/community-join-requirements-tokens-lost
                                                :else (if is-sufficient?
                                                        :t/community-join-requirements-met
                                                        :t/community-join-requirements-not-met))
                                              (= gate-type :read)
                                              (if is-sufficient?
                                                :t/community-channel-read-requirements-met
                                                :t/community-channel-read-requirements-not-met)
                                              (= gate-type :write)
                                              (if is-sufficient?
                                                :t/community-channel-write-requirements-met
                                                :t/community-channel-write-requirements-not-met))]
    [rn/view
     [rn/view {:style (get styles :token-requirement-text-spacing)}
      [text/text
       {:weight          :medium
        :number-of-lines 1}
       (i18n/label
        message-label)]
      [text/text
       {:size            :paragraph-2
        :number-of-lines 1}
       (i18n/label
        you-must-hold-label)]]

     (if multiple-token-requirements?
       (map-indexed
        (fn [token-requirement-index tokens]
          ^{:key token-requirement-index}
          [rn/view
           {:style (merge
                    (get styles :token-row-container)
                    (when-not (= token-requirement-index 0)
                      (get styles :token-row-container-no-shift)))}
           (when-not (= token-requirement-index 0)
             [text/text
              {:style (get styles :token-row-or-text)
               :size  :paragraph-2} "or"])
           [token-requirement-list-row tokens community-color]])
        token-requirement-lists)
       [rn/view {:style (get styles :token-row-container)}
        [token-requirement-list-row token-requirement-lists community-color]])]))

(defn is-community-locked?
  [community]
  (not (are-multiple-token-requirements-met? (get-in community [:gates :join]))))

(defn is-channel-locked?
  [channel]
  (not (are-multiple-token-requirements-met?
        (or (get-in channel [:gates :read])
            (get-in channel [:gates :write])))))

(defn token-gating
  "[token-gating opts]
   opts
   {
     :community {
       :name string
       :community-avatar-img-src string
       :community-color string
       :community-text-color string
       :token-requirements-changed? boolean
       :required-tokens-lost? boolean
     }
     :channel {
       :name string
       :emoji string
       :emoji-background-color :string
       :on-enter-channel callback
       :membership-request-denied? boolean
     }
   }"
  [_ _]
  (fn [{:keys [community channel]}]
    (let [type (if (some? community) :community :channel)
          {:keys [name
                  emoji
                  emoji-background-color
                  on-enter-channel
                  community-avatar-img-src
                  community-color
                  community-text-color
                  membership-request-denied?
                  token-requirements-changed?
                  required-tokens-lost?
                  gates]}
          (if (= type :community) community channel)
          locked? (if
                    (= type :community)
                    (is-community-locked? community)
                    (is-channel-locked? channel))]

      [rn/view
       {:style (merge (get styles :container)
                      {:background-color (colors/theme-colors colors/white colors/neutral-90)})}
       [rn/view
        {:style (merge
                 (get styles :header-container)
                 (if
                   (= type :community)
                   (get styles :header-spacing-community)
                   (get styles :header-spacing-channel)))}

        [rn/view {:style (get styles :header-avatar)}
         (if (= type :community)
           [fast-image/fast-image
            {:source community-avatar-img-src
             :style  (get styles :header-community-avatar-image)}]

           [channel-avatar/channel-avatar
            {:big?                   true
             :locked?                locked?
             :emoji                  emoji
             :emoji-background-color emoji-background-color}])]

        [rn/view {:style (get styles :header-title-container)}
         [text/text
          {:weight          :semi-bold
           :number-of-lines 1
           :size            :heading-1
           :style           (get styles :header-text)} (if (= type :community) name (str "# " name))]

         (when (= type :community)
           [icon/icon
            (if locked?
              :main-icons2/locked
              :main-icons2/unlocked)
            {:container-style (get styles :header-title-lock)
             :color           (colors/theme-colors
                               colors/neutral-50
                               colors/neutral-40)}])]

        [button/button
         {:type  :outline
          :size  32
          :icon  true
          :style (get styles :header-info-button)} :main-icons2/info]]

       (map-indexed (fn [gate-index gate]
                      ^{:key gate-index}
                      [:<>
                       (when-not (= gate-index 0)
                         [rn/view
                          {:style (merge
                                   (get styles :divider)
                                   {:background-color (colors/theme-colors
                                                       colors/neutral-10
                                                       colors/neutral-80)})}])
                       [token-requirement-list
                        {:gate                        gate
                         :token-requirements-changed? token-requirements-changed?
                         :required-tokens-lost?       required-tokens-lost?} community-color]])
                    gates)

       (when (= type :channel)
         (if membership-request-denied?
           [information-box/information-box
            {:type           :error
             :icon           :main-icons2/untrustworthy
             :no-icon-color? true
             :style          (get styles :membership-request-denied)}
            (i18n/label
             :t/membership-request-denied)]

           [:<>
            [button/button
             {:type                 :community
              :community-color      community-color
              :community-text-color community-text-color
              :style                (get styles :enter-button)
              :disabled             locked?
              :on-press             on-enter-channel}
             (str "# "
                  (i18n/label
                   :t/enter-channel))]
            [text/text
             {:size  :paragraph-2
              :style (merge
                      (get styles :info-text)
                      {:color (colors/theme-colors
                               colors/neutral-50
                               colors/neutral-40)})}
             (i18n/label
              :t/community-enter-channel-info)]]))])))
