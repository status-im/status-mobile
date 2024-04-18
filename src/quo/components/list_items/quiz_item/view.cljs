(ns quo.components.list-items.quiz-item.view
  (:require
    [quo.components.icon :as icon]
    [quo.components.list-items.quiz-item.style :as style]
    [quo.components.markdown.text :as text]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [utils.i18n :as i18n]))

(defn view
  [{:keys [state number word on-press] :as props}]
  (let [theme (quo.theme/use-theme)]
    [rn/pressable
     {:style    (style/container props theme)
      :on-press on-press}
     (if (or (= state :empty) (= state :disabled))
       [rn/view
        {:style               (style/num-container props theme)
         :accessibility-label :number-container}
        [text/text {:weight :semi-bold} number]]
       [text/text {:style (style/text props theme)}
        (if (= state :success) word (i18n/label :t/oops-wrong-word))])
     (when (= state :success)
       [icon/icon :i/check
        {:color               (colors/theme-colors colors/success-50 colors/success-60 theme)
         :accessibility-label :success-icon}])
     (when (= state :error)
       [icon/icon :i/incorrect
        {:color               (colors/theme-colors colors/danger-50 colors/danger-60 theme)
         :accessibility-label :error-icon}])]))
