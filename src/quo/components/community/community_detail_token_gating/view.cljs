(ns quo.components.community.community-detail-token-gating.view
  (:require [clojure.string :as string]
            [quo.components.community.community-detail-token-gating.style :as style]
            [quo.components.dividers.divider-label.view :as divider-label]
            [quo.components.markdown.text :as text]
            [quo.components.tags.collectible-tag.view :as collectible-tag]
            [quo.components.tags.token-tag.view :as token-tag]
            [quo.foundations.colors :as colors]
            [quo.theme]
            [react-native.core :as rn]
            [utils.i18n :as i18n]))

(defn- token-view
  [{:keys [collectible? img-src amount sufficient?] :as token}]
  (let [token-symbol (:symbol token)]
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
        :options       (when sufficient? :hold)}])))

(defn- tokens-row
  [{:keys [tokens divider? first?]}]
  (let [theme (quo.theme/use-theme)]
    [:<>
     [rn/view
      {:style (style/token-row first?)}
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
         (string/lower-case (i18n/label :t/or))]])]))

(defn- role-view
  [{:keys [role tokens satisfied? role-text]}]
  (when (seq tokens)
    ^{:key (str "role-" role)}
    [:<>
     [text/text
      {:size   :paragraph-1
       :weight :medium
       :style  {:margin-top 4}}
      (if satisfied?
        (i18n/label :t/you-eligible-to-join-as {:role role-text})
        (i18n/label :t/you-not-eligible-to-join-as {:role role-text}))]
     [text/text
      {:size  :paragraph-2
       :style {:margin-bottom 8}}
      (i18n/label (if satisfied? :t/you-hodl :t/you-must-hold))]

     (map-indexed (fn [index tokens-item]
                    ^{:key (str role "-tokens-" index)}
                    [tokens-row
                     {:tokens   tokens-item
                      :first?   (zero? index)
                      :divider? (= index (dec (count tokens)))}])
                  tokens)]))

(defn view
  [{:keys [permissions]}]
  [rn/view {:style style/container}
   (map role-view permissions)])
