import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = 'http://host.docker.internal:8080';

export const options = {
    stages: [
        { duration: '20s', target: 10 },
        { duration: '30s', target: 50 },
        { duration: '30s', target: 100 },
        { duration: '20s', target: 0 },
    ],

    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<1000'],
    },
};

export function setup() {
    const pautaResponse = http.post(
        `${BASE_URL}/api/v1/pautas`,
        JSON.stringify({
            titulo: 'Pauta teste de performance',
            descricao: 'Pauta criada automaticamente pelo k6',
        }),
        {
            headers: {
                'Content-Type': 'application/json',
            },
        }
    );

    check(pautaResponse, {
        'pauta criada com sucesso': (response) => response.status === 201,
    });

    const pauta = pautaResponse.json();

    const sessaoResponse = http.post(
        `${BASE_URL}/api/v1/pautas/${pauta.id}/sessoes`,
        JSON.stringify({
            duracaoMinutos: 10,
        }),
        {
            headers: {
                'Content-Type': 'application/json',
            },
        }
    );

    check(sessaoResponse, {
        'sessao criada com sucesso': (response) => response.status === 201,
    });

    return {
        pautaId: pauta.id,
    };
}

export default function (data) {
    const associadoId = `ASSOCIADO-${__VU}-${__ITER}`;

    const opcao = __ITER % 2 === 0
        ? 'SIM'
        : 'NAO';

    const response = http.post(
        `${BASE_URL}/api/v1/pautas/${data.pautaId}/votos`,
        JSON.stringify({
            associadoId: associadoId,
            opcao: opcao,
        }),
        {
            headers: {
                'Content-Type': 'application/json',
            },
        }
    );

    check(response, {
        'voto registrado': (response) => response.status === 201,
    });

    sleep(0.1);
}