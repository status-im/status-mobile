(ns status-im.contexts.preview.quo.common
  (:require
    [quo.core :as quo]
    [quo.theme :as quo.theme]
    [utils.re-frame :as rf]))


(defn navigation-bar
  [{:keys [title]}]
  (let [theme         (quo.theme/use-theme)
        logged-in?    (rf/sub [:multiaccount/logged-in?])
        has-profiles? (boolean (rf/sub [:profile/profiles-overview]))
        root          (if has-profiles? :screen/profile.profiles :screen/onboarding.intro)
        light?        (= theme :light)]
    [quo/page-nav
     {:type       :title
      :title      title
      :text-align :left
      :icon-name  :i/close
      :right-side [{:icon-name (if light? :i/dark :i/light)
                    :on-press  #(if light? (quo.theme/set-theme :dark) (quo.theme/set-theme :light))}]
      :on-press   #(if (or logged-in? (not= (rf/sub [:view-id]) :quo-preview))
                     (rf/dispatch [:navigate-back])
                     (do
                       (quo.theme/set-theme :dark)
                       (rf/dispatch [:init-root root])))}]))
