(ns quo.components.community.community-token-gating.view
  (:require [clojure.string :as string]
            [quo.components.buttons.button.view :as button]
            [quo.components.community.community-token-gating.schema :as component-schema]
            [quo.components.community.community-token-gating.style :as style]
            [quo.components.dividers.divider-label.view :as divider-label]
            [quo.components.icon :as icon]
            [quo.components.markdown.text :as text]
            [quo.components.tags.collectible-tag.view :as collectible-tag]
            [quo.components.tags.token-tag.view :as token-tag]
            [quo.foundations.colors :as colors]
            [quo.theme :as theme]
            [react-native.core :as rn]
            [schema.core :as schema]
            [utils.i18n :as i18n]))

(defn- token-view
  [{:keys [collectible? img-src amount sufficient?] :as token}]
  (let [token-symbol (:symbol token)]
    [rn/view {:style style/token-wrapper}
     (if collectible?
       [collectible-tag/view
        {:collectible-name    token-symbol
         :size                :size-24
         :collectible-img-src img-src
         :options             (when sufficient?
                                :hold)}]
       [token-tag/view
        {:token-symbol  token-symbol
         :size          :size-24
         :token-value   amount
         :token-img-src img-src
         :options       (when sufficient? :hold)}])]))

(defn- tokens-row
  [{:keys [theme tokens divider?]}]
  [:<>
   [rn/view
    {:style style/token-row}
    (map-indexed (fn [token-index token]
                   ^{:key (str "token-" token-index)}
                   [token-view token])
                 tokens)]
   (when-not divider?
     [divider-label/view
      {:container-style style/divider}
      [text/text
       {:size  :label
        :style {:color (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)}}
       (string/lower-case (i18n/label :t/or))]])])

(defn- view-internal
  [{:keys [tokens community-color role satisfied? on-press on-press-info]}]
  (let [theme            (theme/use-theme)
        last-token-index (dec (count tokens))]
    [rn/view {:style (style/container theme)}
     [rn/view {:style style/eligibility-row}
      [text/text
       {:size   :paragraph-1
        :weight :medium
        :style  style/eligibility-label}
       (if satisfied?
         (i18n/label :t/you-eligible-to-join-as {:role role})
         (i18n/label :t/you-not-eligible-to-join))]
      [rn/pressable {:on-press on-press-info}
       [icon/icon :i/info
        {:color               (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)
         :accessibility-label :community-token-gating-info}]]]
     [text/text
      {:size  :paragraph-2
       :style style/you-hodl}
      (i18n/label (if satisfied? :t/you-hodl :t/you-must-hold))]
     (map-indexed (fn [index tokens-item]
                    ^{:key (str role "-tokens-" index)}
                    [tokens-row
                     {:tokens   tokens-item
                      :theme    theme
                      :divider? (= index last-token-index)}])
                  tokens)
     [button/button
      {:on-press            on-press
       :container-style     style/join-button
       :accessibility-label :join-community-button
       :customization-color community-color
       :disabled?           (not satisfied?)
       :icon-left           (if satisfied? :i/unlocked :i/locked)}
      (i18n/label :t/request-to-join)]]))

(def view (schema/instrument #'view-internal component-schema/?schema))
