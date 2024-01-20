(ns quo.components.community.token-gating
  (:require
    [clojure.string :as string]
    [quo.components.community.style :as style]
    [quo.components.markdown.text :as text]
    [quo.components.tags.token-tag.view :as token-tag]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [utils.i18n :as i18n]))

(defn token-requirement-list-row
  [tokens padding?]
  [rn/view {:style (style/token-row padding?)}
   (map-indexed (fn [token-index {:keys [img-src amount sufficient? purchasable?] :as token}]
                  ^{:key token-index}
                  [rn/view {:style style/token-tag-spacing}
                   [token-tag/view
                    {:token-symbol  (:symbol token)
                     :token-img-src img-src
                     :token-value   amount
                     :size          :size-24
                     :options       (cond
                                      sufficient?  :hold
                                      purchasable? :add)}]])
                tokens)])

(defn- internal-view
  [{:keys [tokens padding? theme]}]
  [:<>
   (if (> (count tokens) 1)
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
      tokens)
     [token-requirement-list-row (first tokens) padding?])])

(def token-requirement-list (quo.theme/with-theme internal-view))
