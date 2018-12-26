(ns illuminepixels.views
  (:require
    [re-frame.core :as rf]
    [illuminepixels.subs :as subs]
    [illuminepixels.routes :as routes]
    [illuminepixels.utils :as utils]
    [clojure.string :as string]))


(defn corner []
  [:a {:href   "https://github.com/rutledgepaulv/illuminepixels",
       :class  "github-corner display-md-up"
       :target "_blank"}
   [:svg {:width "60", :height "60", :viewBox "0 0 250 250"}
    [:path {:d "M0,0 L115,115 L130,115 L142,142 L250,250 L250,0 Z"}]
    [:path {:d "M128.3,109.0 C113.8,99.7 119.0,89.6 119.0,89.6 C122.0,82.7 120.5,78.6 120.5,78.6 C119.2,72.0 123.4,76.3 123.4,76.3 C127.3,80.9 125.5,87.3 125.5,87.3 C122.9,97.6 130.6,101.9 134.4,103.2", :fill "currentColor", :style {:transform-origin "130px 106px"}, :class "octo-arm"}]
    [:path {:d "M115.0,115.0 C114.9,115.1 118.7,116.5 119.8,115.4 L133.7,101.6 C136.9,99.2 139.9,98.4 142.2,98.6 C133.8,88.0 127.5,74.4 143.8,58.0 C148.5,53.4 154.0,51.2 159.7,51.0 C160.3,49.4 163.2,43.6 171.4,40.1 C171.4,40.1 176.1,42.5 178.8,56.2 C183.1,58.6 187.2,61.8 190.9,65.4 C194.5,69.0 197.7,73.2 200.1,77.6 C213.8,80.2 216.3,84.9 216.3,84.9 C212.7,93.1 206.9,96.0 205.4,96.6 C205.1,102.4 203.0,107.8 198.3,112.5 C181.9,128.9 168.3,122.5 157.7,114.1 C157.9,116.9 156.7,120.9 152.7,124.9 L141.0,136.5 C139.8,137.7 141.6,141.9 141.8,141.8 Z", :fill "currentColor", :class "octo-body"}]]])


(defn navigation []
  (let [{:keys [path]} @(rf/subscribe [::subs/active-route])
        nav-items {:blogs-panel "blog"
                   :games-panel "games"
                   :about-panel "about"}
        home      (routes/view->path :home-panel)]
    [:nav
     [:div.nav-container
      [:div.nav-logo [:a {:href home} "home"]]
      [:ul.nav-links
       (for [[k v] nav-items]
         (let [target (routes/view->path k)]
           (if (string/starts-with? path target)
             [:a.active {:key target :href target} [:h6 v]]
             [:a {:key target :href target} [:h6 v]])))]]]))


(defn peer-display []
  (let [{:keys [path]} @(rf/subscribe [::subs/active-route])
        {:keys [peers]} @(rf/subscribe [::subs/subscribe {:kind :peers :route path}])]
    [:span (str peers " " (if (> peers 1) "viewers" "viewer"))]))

(defn home-panel []
  [:div.row
   [:div.col.col-md-6.col-md-offset-3
    [:div.panel
     [:div.panel-body
      [:div.row
       [:div.col.col-md-8
        [:div.panel
         [:div.panel-body
          [:div
           [:h6 [:em "Illumine"]]
           [:p "To enlighten intellectually or spiritually; enable to understand"]]]]]
       [:div.col.col-md-4
        [:div.panel
         [:div.panel-body
          [:div
           [:h6 [:em "Pixels"]]
           [:p "An atomic element of display"]]]]]]
      [:section
       [:p {:style {:text-align "center"}} "This is a playground and technical blog brought to you by "
        [:a {:href (routes/view->path :about-panel)} "Paul Rutledge"]
        "."]]]
     [:div.panel-footer
      [:div {:style {:float "right"}} [peer-display]]
      [:div [:span "(ノಠ益ಠ)ノ彡┻━┻"]]]]]])

(defn blogs-panel []
  (let [blogs @(rf/subscribe [::subs/blogs])]
    [:section
     [:div.row
      (for [{{:keys [slug title summary created]} :metadata} blogs]
        (let [timestamp (get created :timestamp)]
          [:div.col.col-md-6 {:key slug}
           [:div.card
            [:h3.card-title
             [:a {:href (routes/view->path :blog-panel {:slug slug})} title]
             [:span {:style {:float "right"}} (utils/format-date timestamp)]]
            summary]]))]]))

(defn games-panel []
  (let [games @(rf/subscribe [::subs/games])]
    [:section
     (for [{:keys [name slug description]} games]
       [:div.row {:key slug}
        [:div.col.col-md-6.col-md-offset-3
         [:div.card
          [:h3.card-title
           [:a {:href (routes/view->path :game-panel {:slug slug})} name]]
          description]]])]))

(defn about-panel []
  (let [about @(rf/subscribe [::subs/about])]
    [:div.row
     [:div.col.col-md-6.col-md-offset-3
      [:div.panel
       [:div.panel-body (:html about)]
       [:div.panel-footer
        [:div {:style {:float "right"}} [peer-display]]
        [:div
         [:a {:href "https://github.com/RutledgePaulV" :target "_blank"} "Github"]
         [:span "  /  "]
         [:a {:href "https://github.com/RutledgePaulV" :target "_blank"} "LinkedIn"]]]]]]))

(defn blog-panel [slug]
  (let [blog @(rf/subscribe [::subs/blog slug])]
    [:section (:html blog)]))

(defn game-panel [slug]
  (let [{:keys [name description]} @(rf/subscribe [::subs/game slug])]
    [:div name]))


(defn main-panel [{:keys [name path-params]}]
  [:div
   [corner]
   [navigation]
   (case name
     :home-panel [home-panel]
     :game-panel [game-panel (get path-params :slug)]
     :games-panel [games-panel]
     :blog-panel [blog-panel (get path-params :slug)]
     :blogs-panel [blogs-panel]
     :about-panel [about-panel]
     [:div "Not found!"])])
