(ns quo.components.inputs.recovery-phrase.view
  (:require
    [clojure.string :as string]
    [quo.components.inputs.recovery-phrase.style :as style]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]))

(def ^:private custom-props
  [:customization-color :theme :blur? :cursor-color :multiline :on-focus :on-blur
   :placeholder-text-color :mark-errors? :error-pred :word-limit])

(defn- error-word
  [text theme]
  [rn/text {:style (style/error-word theme)}
   text])

(defn- mark-error-words
  [{:keys [pred-last-word pred-previous-words text word-limit theme]}]
  (let [last-index (dec (count (string/split text #"\s+")))
        words      (map #(apply str %)
                        (partition-by #(= " " %) text))]
    (->> words
         (reduce (fn [{:keys [idx] :as acc} word]
                   (let [error-pred        (if (= last-index idx) pred-last-word pred-previous-words)
                         invalid-word?     (and (or (error-pred word)
                                                    (>= idx word-limit))
                                                (not (string/blank? word)))
                         not-blank-spaces? (not (string/blank? word))]
                     (cond-> acc
                       not-blank-spaces? (update :idx inc)
                       :always           (update :result
                                                 conj
                                                 (if invalid-word?
                                                   [error-word word theme]
                                                   word)))))
                 {:result [:<>]
                  :idx    0})
         :result)))

(defn recovery-phrase-input
  [{:keys [customization-color blur? on-focus on-blur mark-errors?
           error-pred-current-word error-pred-written-words word-limit
           container-style placeholder-text-color on-input-ref]
    :or   {customization-color      :blue
           word-limit               ##Inf
           error-pred-current-word  (constantly false)
           error-pred-written-words (constantly false)}
    :as   props}
   text]
  (let [theme             (quo.theme/use-theme)
        [state set-state] (rn/use-state :default)
        on-focus          (rn/use-callback
                           (fn []
                             (set-state :focused)
                             (when on-focus (on-focus))))
        on-blur           (rn/use-callback
                           (fn []
                             (set-state :default)
                             (when on-blur (on-blur))))
        extra-props       (apply dissoc props custom-props)]
    [rn/view {:style (style/container container-style)}
     [rn/text-input
      (merge {:ref                    on-input-ref
              :accessibility-label    :recovery-phrase-input
              :style                  (style/input theme)
              :placeholder-text-color (or placeholder-text-color
                                          (style/placeholder-color state theme blur?))
              :cursor-color           (style/cursor-color customization-color theme)
              :keyboard-appearance    theme
              :multiline              true
              :on-focus               on-focus
              :on-blur                on-blur}
             extra-props)
      (if mark-errors?
        (mark-error-words {:pred-last-word      error-pred-current-word
                           :pred-previous-words error-pred-written-words
                           :text                text
                           :word-limit          word-limit
                           :theme               theme})
        text)]]))
