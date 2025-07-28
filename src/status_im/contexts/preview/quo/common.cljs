(ns status-im.contexts.preview.quo.common
  (:require
    [quo.context :as quo.context]
    [quo.core :as quo]
    [utils.re-frame :as rf]))


(defn navigation-bar
  [{:keys [title]}]
  (let [theme         (quo.context/use-theme)
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
                    :on-press  #(if light?
                                  (rf/dispatch [:theme/switch {:theme :dark}])
                                  (rf/dispatch [:theme/switch {:theme :light}]))}]
      :on-press   #(if (or logged-in? (not= (rf/sub [:view-id]) :screen/quo-preview))
                     (rf/dispatch [:navigate-back])
                     (do
                       (rf/dispatch [:theme/switch {:theme :dark}])
                       (rf/dispatch [:update-theme-and-init-root root])))}]))
