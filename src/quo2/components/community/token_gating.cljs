(ns quo2.components.community.token-gating
  (:require [quo2.components.markdown.text :as text]
            [quo2.components.tags.token-tag :as token-tag]
            [react-native.core :as rn]
            [utils.i18n :as i18n]
            [clojure.string :as string]
            [quo2.theme :as quo.theme]
            [quo2.components.community.style :as style]))

(defn token-requirement-list-row
  [tokens padding?]
  [rn/view
   {:style (style/token-row padding?)}
   (doall
    (map-indexed (fn [token-index token]
                   (let [{:keys [img-src amount sufficient? purchasable? loading?]} token]
                     ^{:key token-index}
                     [rn/view {:style style/token-tag-spacing}
                      [token-tag/token-tag
                       {:symbol      (:symbol token)
                        :value       amount
                        :size        24
                        :sufficient? sufficient?
                        :purchasable purchasable?
                        :loading?    loading?
                        :img-src     img-src}]]))
                 tokens))])

(defn- internal-view
  [{:keys [tokens padding? theme]}]
  [:<>
   (if (> (count tokens) 1)
     (doall
      (map-indexed
       (fn [token-requirement-index tokens]
         ^{:key token-requirement-index}
         [rn/view {:margin-bottom 12}
          (when-not (= token-requirement-index 0)
            [rn/view {:style (style/token-row-or-border theme)}])
          (when-not (= token-requirement-index 0)
            [text/text
             {:style (style/token-row-or-text padding? theme)
              :size  :label} (string/lower-case (i18n/label :t/or))])
          [token-requirement-list-row tokens padding?]])
       tokens))
     [token-requirement-list-row (first tokens) padding?])])

(def token-requirement-list (quo.theme/with-theme internal-view))
