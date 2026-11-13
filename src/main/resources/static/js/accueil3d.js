// Pictogramme Lylybot en 3D (Three.js) pour le hero de la page d'accueil.
// Three.js est servi en local : static/vendor/js/three.module.min.js
import * as THREE from "../vendor/js/three.module.min.js";
import { FORME } from "./logo-forme.js";

const conteneur = document.getElementById("logo3d");
const canvas = conteneur?.querySelector("canvas");
const mouvementReduit = window.matchMedia("(prefers-reduced-motion: reduce)").matches;

function demarrer() {
    // --- Rendu ---------------------------------------------------------
    const renderer = new THREE.WebGLRenderer({ canvas, antialias: true, alpha: true });
    renderer.setClearColor(0x000000, 0);
    renderer.toneMapping = THREE.ACESFilmicToneMapping;
    renderer.toneMappingExposure = 1.5;

    const scene = new THREE.Scene();
    const camera = new THREE.PerspectiveCamera(28, 1, 0.1, 50);
    camera.position.set(0, 0, 5.4);

    // --- Géométrie : on extrude les contours du logo ----------------------
    const formes = FORME.map((f) => {
        const forme = new THREE.Shape(paires(f.ex));
        f.trous.forEach((t) => forme.holes.push(new THREE.Path(paires(t))));
        return forme;
    });
    const geometrie = new THREE.ExtrudeGeometry(formes, {
        depth: 0.34,
        curveSegments: 1,
        bevelEnabled: true,
        bevelThickness: 0.035,
        bevelSize: 0.03,
        bevelSegments: 3,
    });
    geometrie.center();

    const materiau = new THREE.MeshStandardMaterial({
        color: 0xf4f4f4,
        roughness: 0.3,
        metalness: 0.2,
    });
    const logo = new THREE.Mesh(geometrie, materiau);
    const groupe = new THREE.Group();
    groupe.add(logo);
    scene.add(groupe);

    // --- Lumières (monochromes, pour rester dans la charte) ---------------
    scene.add(new THREE.AmbientLight(0xffffff, 1.2));
    const cle = new THREE.DirectionalLight(0xffffff, 5);
    cle.position.set(3, 4, 6);
    scene.add(cle);
    const contre = new THREE.DirectionalLight(0xffffff, 3.5);
    contre.position.set(-5, 1, -4);
    scene.add(contre);
    const remplissage = new THREE.PointLight(0xffffff, 30, 20);
    remplissage.position.set(-3, -3, 4);
    scene.add(remplissage);

    // --- Taille ----------------------------------------------------------
    function redimensionner() {
        const { clientWidth: l, clientHeight: h } = conteneur;
        if (!l || !h) return;
        renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
        renderer.setSize(l, h, false);
        camera.aspect = l / h;
        camera.updateProjectionMatrix();
        rendreUneFois();
    }
    new ResizeObserver(redimensionner).observe(conteneur);

    // --- Interaction : le logo suit doucement la souris -------------------
    const cible = { x: 0, y: 0 };
    const courant = { x: 0, y: 0 };
    if (!mouvementReduit) {
        window.addEventListener("pointermove", (e) => {
            cible.y = (e.clientX / window.innerWidth - 0.5) * 0.9;
            cible.x = (e.clientY / window.innerHeight - 0.5) * 0.5;
        });
    }

    // --- Animation -------------------------------------------------------
    let visible = true;
    let enCours = false;
    const debut = performance.now();

    function poser(s) {
        const entree = Math.min(1, s / 1.6);
        const facile = 1 - Math.pow(1 - entree, 3); // easeOutCubic
        courant.x += (cible.x - courant.x) * 0.06;
        courant.y += (cible.y - courant.y) * 0.06;

        groupe.rotation.y = (1 - facile) * -1.4 + Math.sin(s * 0.6) * 0.42 + courant.y;
        groupe.rotation.x = Math.sin(s * 0.45) * 0.07 + courant.x;
        groupe.position.y = Math.sin(s * 0.9) * 0.05;
        groupe.scale.setScalar(0.82 + 0.18 * facile);
    }

    function rendreUneFois() {
        if (!mouvementReduit) return; // la boucle s'en charge
        groupe.rotation.set(-0.06, 0.38, 0);
        renderer.render(scene, camera);
    }

    function boucle() {
        if (!visible || document.hidden) {
            enCours = false;
            return;
        }
        poser((performance.now() - debut) / 1000);
        renderer.render(scene, camera);
        requestAnimationFrame(boucle);
    }
    function lancer() {
        if (mouvementReduit || enCours) return;
        enCours = true;
        requestAnimationFrame(boucle);
    }

    new IntersectionObserver(([e]) => {
        visible = e.isIntersecting;
        if (visible) lancer();
    }).observe(conteneur);
    document.addEventListener("visibilitychange", lancer);

    redimensionner();
    if (mouvementReduit) {
        rendreUneFois();
    } else {
        poser(0);
        renderer.render(scene, camera);
        lancer();
    }
    conteneur.classList.add("pret");
}

function paires(plat) {
    const pts = [];
    for (let i = 0; i < plat.length; i += 2) pts.push(new THREE.Vector2(plat[i], plat[i + 1]));
    return pts;
}

if (conteneur && canvas) {
    try {
        demarrer();
    } catch (erreur) {
        // WebGL indisponible : on garde l'image fixe
        console.warn("Scène 3D désactivée :", erreur);
        conteneur.classList.add("sans-webgl");
    }
}
