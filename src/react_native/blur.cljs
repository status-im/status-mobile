(ns react-native.blur
  (:require ["@react-native-community/blur" :as blur]
            [react-native.platform :as platform]
            [reagent.core :as reagent]
            [react-native.core :as rn]
            ["react-native-webview" :default rn-webview]))

(def view (reagent/adapt-react-class (.-BlurView blur)))

(def webview-class
  (reagent/adapt-react-class rn-webview))

(defn webview-blur
  [{:keys [style blur-radius overlay-color]
    :or   {style         {}
           blur-radius   10
           overlay-color "#00000000"}}
   children]
  (let
    [html
     (str
      "<html>
            <head>
              <meta name=\"viewport\" content=\"initial-scale=1.0 maximum-scale=1.0\" />
              <style>
                .blur {
                  position: absolute;
                  top: 0;
                  right: 0;
                  bottom: 0;
                  left: 0;
                  background-color: "
      overlay-color
      ";
                  -webkit-backdrop-filter: blur("
      blur-radius
      "px);
                  backdrop-filter: blur("
      blur-radius
      "px);
                }
              </style>
              <script type=\"text/javascript\">  
                  //alert (\"This is an alert dialog box\");  
              </script>  
            </head>
            <body>
              <div class=\"blur\" />
            </body>
          </html>")]
    (reagent/as-element
     [rn/view {:style style}
      [webview-class
       {:style {:position :absolute :top 0 :left 0 :right 0 :bottom 0 :background-color :transparent}
        :source {:html html}
        :showsHorizontalScrollIndicator false
        :pointer-events :none}]
      children])))

;; blur view is currently not working correctly on Android.
(def ios-view (if platform/ios? view rn/view))
