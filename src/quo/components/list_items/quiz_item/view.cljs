(ns quo.components.list-items.quiz-item.view
  (:require
    [quo.components.icon :as icon]
    [quo.components.list-items.quiz-item.style :as style]
    [quo.components.markdown.text :as text]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [utils.i18n :as i18n]))


(defn- view-internal
  [{:keys [state theme number word] :as props}]
  [rn/view {:style (style/container props)}
   (if (or (= state :empty) (= state :disabled))
     [rn/view
      {:style               (style/num-container props)
       :accessibility-label :number-container}
      [text/text {:weight :semi-bold} number]]
     [text/text {:style (style/text props)}
      (if (= state :success) word (i18n/label :t/ops))])
   (when (= state :success)
     [icon/icon :i/check
      {:color               (colors/theme-colors colors/success-50 colors/success-60 theme)
       :accessibility-label :success-icon}])
   (when (= state :error)
     [icon/icon :i/incorrect
      {:color               (colors/theme-colors colors/danger-50 colors/danger-60 theme)
       :accessibility-label :error-icon}])])

(def view (quo.theme/with-theme view-internal))
