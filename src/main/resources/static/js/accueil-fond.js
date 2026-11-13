// Fond animé de la page d'accueil : un shader GLSL plein écran (Three.js).
// Bruit fractal déformé + lignes de niveau, en niveaux de gris très discrets.
import * as THREE from "../vendor/js/three.module.min.js";

const canvas = document.getElementById("fond-shader");
const mouvementReduit = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
const ECHELLE = 0.6; // résolution interne : le fond est flou par nature, inutile de calculer plus

const vertex = /* glsl */ `
    void main() { gl_Position = vec4(position.xy, 0.0, 1.0); }
`;

const fragment = /* glsl */ `
    precision highp float;
    uniform float uTime;
    uniform vec2 uRes;
    uniform vec2 uMouse;

    float hash(vec2 p) {
        p = fract(p * vec2(123.34, 456.21));
        p += dot(p, p + 45.32);
        return fract(p.x * p.y);
    }
    float noise(vec2 p) {
        vec2 i = floor(p), f = fract(p);
        f = f * f * (3.0 - 2.0 * f);
        return mix(mix(hash(i), hash(i + vec2(1.0, 0.0)), f.x),
                   mix(hash(i + vec2(0.0, 1.0)), hash(i + vec2(1.0, 1.0)), f.x), f.y);
    }
    float fbm(vec2 p) {
        float v = 0.0, a = 0.5;
        for (int i = 0; i < 5; i++) {
            v += a * noise(p);
            p = p * 2.02 + vec2(17.3, 9.1);
            a *= 0.5;
        }
        return v;
    }

    void main() {
        vec2 uv = (gl_FragCoord.xy - 0.5 * uRes) / uRes.y;
        vec2 m = (uMouse - 0.5) * vec2(uRes.x / uRes.y, 1.0);
        float t = uTime * 0.045;

        // Déformation de domaine : le bruit est plié par lui-même, ce qui donne un flux organique
        vec2 p = uv * 1.5;
        vec2 q = vec2(fbm(p + vec2(0.0, t)), fbm(p + vec2(5.2, 1.3) - t));
        vec2 r = vec2(fbm(p + 3.0 * q + vec2(1.7, 9.2) + t * 1.5),
                      fbm(p + 3.0 * q + vec2(8.3, 2.8) - t));
        float f = fbm(p + 3.0 * r + m * 0.35);

        // Lignes de niveau fines (effet topographique)
        float x = f * 10.0;
        float d = abs(fract(x) - 0.5);
        float ligne = 1.0 - smoothstep(0.0, fwidth(x) * 1.2, d);

        float masse = smoothstep(0.25, 0.85, f);
        float lum = masse * 0.085 + ligne * 0.06;

        // Plus lumineux autour du pictogramme (à droite), plus sombre sur le texte (à gauche)
        float zoneLogo = smoothstep(1.2, 0.1, length(uv - vec2(0.38, 0.0)));
        lum *= 0.45 + 1.1 * zoneLogo;

        // Halo qui suit la souris
        lum += 0.05 * smoothstep(0.55, 0.0, length(uv - m));

        // Vignettage + grain léger
        lum *= smoothstep(1.5, 0.3, length(uv));
        lum += (hash(gl_FragCoord.xy + fract(uTime)) - 0.5) * 0.012;

        gl_FragColor = vec4(vec3(max(lum, 0.0)), 1.0);
    }
`;

function demarrer() {
    const renderer = new THREE.WebGLRenderer({ canvas, antialias: false, alpha: false, powerPreference: "low-power" });
    renderer.setPixelRatio(1);

    const uniforms = {
        uTime: { value: 0 },
        uRes: { value: new THREE.Vector2(1, 1) },
        uMouse: { value: new THREE.Vector2(0.5, 0.5) },
    };
    const materiau = new THREE.ShaderMaterial({ vertexShader: vertex, fragmentShader: fragment, uniforms, depthTest: false, depthWrite: false });
    const scene = new THREE.Scene();
    scene.add(new THREE.Mesh(new THREE.PlaneGeometry(2, 2), materiau));
    const camera = new THREE.Camera();

    function redimensionner() {
        const l = Math.max(2, Math.round(window.innerWidth * ECHELLE));
        const h = Math.max(2, Math.round(window.innerHeight * ECHELLE));
        renderer.setSize(l, h, false);
        uniforms.uRes.value.set(l, h);
        if (mouvementReduit) renderer.render(scene, camera);
    }
    window.addEventListener("resize", redimensionner);

    const cible = new THREE.Vector2(0.5, 0.5);
    if (!mouvementReduit) {
        window.addEventListener("pointermove", (e) => cible.set(e.clientX / window.innerWidth, 1 - e.clientY / window.innerHeight));
    }

    let enCours = false;
    const debut = performance.now();
    function boucle() {
        if (document.hidden) {
            enCours = false;
            return;
        }
        uniforms.uTime.value = (performance.now() - debut) / 1000;
        uniforms.uMouse.value.lerp(cible, 0.04);
        renderer.render(scene, camera);
        requestAnimationFrame(boucle);
    }
    function lancer() {
        if (mouvementReduit || enCours) return;
        enCours = true;
        requestAnimationFrame(boucle);
    }
    document.addEventListener("visibilitychange", lancer);

    redimensionner();
    uniforms.uTime.value = mouvementReduit ? 12 : 0;
    renderer.render(scene, camera);
    lancer();
    document.documentElement.classList.add("fond-pret");
}

if (canvas) {
    try {
        demarrer();
    } catch (erreur) {
        console.warn("Fond shader désactivé :", erreur); // repli : halo CSS
    }
}
